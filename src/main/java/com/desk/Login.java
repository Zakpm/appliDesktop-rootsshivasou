package com.desk;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Login extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login Rootsshivasou");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(30);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        Text sceneTitle = new Text("Rootsshivasou");
        sceneTitle.setTextAlignment(TextAlignment.JUSTIFY);
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 30));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label userName = new Label("Email/Pseudo:");
        grid.add(userName, 0, 1);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Label pw = new Label("Mot de passe:");
        grid.add(pw, 0, 2);

        PasswordField passwordBox = new PasswordField();
        grid.add(passwordBox, 1, 2);

        Button btn = new Button("Connexion");
        btn.getStyleClass().add("btn-primary");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);

        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        

        btn.setOnAction(event -> {
            try {
                String username = userTextField.getText();
                String password = passwordBox.getText();

                if (username.isEmpty() || password.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de formulaire", "Veuillez remplir tous les champs");
                    return;
                }

                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setLogin(username);
                loginRequest.setPassword(password);

                ObjectMapper objectMapper = new ObjectMapper();
                String requestBody = objectMapper.writeValueAsString(loginRequest);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:9982/auth/login")) // URL de votre API
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenAccept(response -> {
                            // Traiter la réponse ici
                            Platform.runLater(() -> {
                                if (response.contains("jwt")) { // Remplacer cette condition par la logique appropriée
                                                                // pour votre réponse
                                    infoBox(Alert.AlertType.CONFIRMATION, "Connexion réussie", null, "Succès");
                                    primaryStage.close(); // Fermez la fenêtre de connexion
                                    openPostsWindow(); // Ouvrez la nouvelle fenêtre pour afficher les posts
                                } else {
                                    showAlert(Alert.AlertType.ERROR, "Échec de la connexion",
                                            "Identifiants incorrects ou problème de serveur");
                                }
                            });
                        })
                        .join();

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur système", "Une erreur est survenue lors de la connexion");
                });
                e.printStackTrace();
            }
        });

        Scene scene = new Scene(grid, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();

        // CSS
        scene.getStylesheets().add(Login.class.getResource("Login.css").toExternalForm());

    }

    private static void showAlert(
            Alert.AlertType alertType,
            String title,
            String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public static void infoBox(
            Alert.AlertType alertType,
            String infoMessage,
            String headerText,
            String title) {
        Alert alert = new Alert(alertType);
        alert.setContentText(infoMessage);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

    private void openPostsWindow() {
        Stage postStage = new Stage();
        postStage.setTitle("Articles");
    
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
    
        Scene scene = new Scene(layout, 1500, 800);
        postStage.setScene(scene);
        postStage.show();
    
        // Au lieu de charger et d'afficher les posts ici, passez la référence de la Stage à la méthode
        loadAndDisplayPosts(layout, postStage);
    }

    private void loadAndDisplayPosts(VBox layout, Stage postStage) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9981/posts"))
                .GET()
                .build();

        GridPane postsContainer = new GridPane();
        postsContainer.setHgap(10); // Espace horizontal entre les cards
        postsContainer.setVgap(10); // Espace vertical entre les cards
        postsContainer.setAlignment(Pos.CENTER); // Centrer le GridPane dans la ScrollPan

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(postsContainer);
        scrollPane.setFitToWidth(true); // Ajuste la largeur du contenu à celle de ScrollPane

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            objectMapper.registerModule(new JavaTimeModule());
                            List<PostDTO> posts = objectMapper.readValue(response, new TypeReference<List<PostDTO>>() {
                            });

                            int column = 0;
                            int row = 0;

                            for (PostDTO post : posts) {
                                VBox postBox = new VBox(10);
                                postBox.getStyleClass().add("card");
                                postBox.setPadding(new Insets(15));
                                postBox.setAlignment(Pos.CENTER); // Centrer les éléments dans la VBox

                                // Ajout du titre
                                Text title = new Text(post.getTitle());
                                title.getStyleClass().add("card-title");
                                title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                                // Affichage tronqué du contenu en texte brut
                                String fullContent = post.getContent();
                                int maxLength = 100; // Longueur maximale du contenu affiché
                                String displayedContent = fullContent.length() > maxLength
                                        ? fullContent.substring(0, maxLength) + "..."
                                        : fullContent;
                                Text content = new Text(displayedContent);
                                content.getStyleClass().add("card-content");
                                content.setWrappingWidth(280); // Ajustez selon la largeur de la VBox

                                // Ajout de l'image (si disponible)
                                if (post.getImage() != null && !post.getImage().isEmpty()) {
                                    String imageUrl = "http://localhost:9981" + post.getImage().get(0);
                                    Image image;
                                    try {
                                        image = new Image(imageUrl, true);
                                    } catch (Exception e) {
                                        URL defaultImageUrl = getClass()
                                                .getResource("/com/desk/images/default-post-image.jpg");
                                        if (defaultImageUrl != null) {
                                            image = new Image(defaultImageUrl.toString());
                                        } else {
                                            System.err.println("Erreur : Image par défaut non trouvée.");
                                            continue;
                                        }
                                    }
                                    ImageView imageView = new ImageView(image);
                                    imageView.getStyleClass().add("card-image");
                                    imageView.setFitWidth(280); // Ajustez selon la largeur de la VBox
                                    imageView.setPreserveRatio(true);
                                    postBox.getChildren().add(imageView);
                                }

                                // Ajout du bouton
                                Button button = new Button("Lire");
                                button.setOnAction(event -> {
                                    int postId = post.getId(); // Obtenez l'ID du post
                                    HttpRequest detailedRequest = HttpRequest.newBuilder()
                                            .uri(URI.create("http://localhost:9981/post/" + postId))
                                            .GET()
                                            .build();

                                    client.sendAsync(detailedRequest, HttpResponse.BodyHandlers.ofString())
                                            .thenApply(HttpResponse::body)
                                            .thenAccept(detailedResponse -> {
                                                Platform.runLater(() -> {
                                                    try {
                                                        PostDTO postDetails = objectMapper.readValue(detailedResponse,
                                                                PostDTO.class);
                                                        showPostDetails(postDetails); // Assurez-vous que cette méthode
                                                                                      // est correctement implémentée
                                                    } catch (JsonProcessingException e) {
                                                        e.printStackTrace();
                                                    }
                                                });
                                            });
                                });

                                button.getStyleClass().add("btn-primary");

                                // Ajout de tous les éléments à la VBox
                                postBox.getChildren().addAll(title, content, button);

                                // Ajout de la VBox au GridPane
                                postsContainer.add(postBox, column, row);

                                // Mise à jour des indices de colonne et de ligne
                                column++;
                                if (column == 3) { // Après 3 éléments, passez à la ligne suivante
                                    column = 0;
                                    row++;
                                }
                            }

                            scrollPane.setContent(postsContainer);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                        // Navbar
                        HBox navbar = new HBox();
                        navbar.getStyleClass().add("navbar");
                        navbar.setAlignment(Pos.CENTER);
                        navbar.setPadding(new Insets(10, 0, 10, 0));

                        Text navbarTitle = new Text("Roots Shivasou");
                        navbarTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                        navbar.getChildren().add(navbarTitle);

                        // Ajouter le navbar et le ScrollPane au layout principal
                        VBox mainLayout = new VBox(navbar, layout);
                        mainLayout.getStyleClass().add("root"); // Utilisez la classe CSS pour le fond

                        // Créer une nouvelle scène avec le mainLayout
                        Platform.runLater(() -> {
                            Scene postScene = new Scene(new VBox(navbar, scrollPane), 1500, 800);

                            // Charger le fichier CSS ici, juste après la création de la scène
                            postScene.getStylesheets().add(Login.class.getResource("Login.css").toExternalForm());

                            // Appliquer la scène à la Stage (fenêtre)
                            postStage.setScene(postScene);

                        });
                                                                                
                    });
                });

        layout.getChildren().add(scrollPane);
    }

    private void showPostDetails(PostDTO postDetails) {
        // Créez une nouvelle fenêtre (Stage) pour afficher les détails
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Détails du Post");

        // Utilisez un VBox comme layout principal pour le contenu
        VBox contentLayout = new VBox(10);
        contentLayout.setAlignment(Pos.CENTER);
        contentLayout.setPadding(new Insets(15, 15, 15, 15));

        // Ajoutez le titre du post
        Text title = new Text(postDetails.getTitle());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Ajoutez le contenu du post
        Text content = new Text(postDetails.getContent());
        content.setWrappingWidth(700); // Ajustez cette valeur selon la taille de votre fenêtre

        // Ajoutez les images du post, si elles existent
        if (postDetails.getImage() != null && !postDetails.getImage().isEmpty()) {
            for (String imageUrl : postDetails.getImage()) {
                try {
                    Image image = new Image("http://localhost:9981" + imageUrl, true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(400); // Ajustez cette valeur selon la taille de votre fenêtre
                    imageView.setPreserveRatio(true);
                    contentLayout.getChildren().add(imageView);
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                }
            }
        }

        // Ajoutez le titre, le contenu et les images au layout de contenu
        contentLayout.getChildren().addAll(title, content);

        // Utilisez un ScrollPane pour rendre le contenu scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(contentLayout);
        scrollPane.setFitToWidth(true); // Ajuste la largeur du contenu à celle du ScrollPane

        // Configurez la scène avec le ScrollPane et affichez la fenêtre
        Scene scene = new Scene(scrollPane, 1000, 600); // Ajustez ces valeurs selon vos besoins
        detailsStage.setScene(scene);
        detailsStage.show();
    }

}