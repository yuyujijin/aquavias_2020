import javafx.application.Application;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.scene.input.KeyEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MenuApplication extends Application {

    private static int WIDTH;
    private static int HEIGHT;
    private static double MUSIQUE;
    private static double SONS;
    Level enCours;
    View v;
    File levelsFolder = new File("levels");
    String [] lvls = levelsFolder.list();
    AnimationTimer at;
    Stage window;

    public List<Pair<String, Runnable>> menuData = Arrays.asList( //Définit une liste qui comprend tous les boutons sous un couple de String et d'action à effectuer
            //Bouton Nouvelle Partie du menu principal
            new Pair<String, Runnable>("Nouvelle Partie", () -> {
                    //stage2.close();
                    try{
                        enCours = new Level(1);
                        window.setScene(new View(enCours));
                    } catch (Exception e){
                        System.out.println("Niveau manquant");
                    }
            }),
            //Bouton continuer du menu principal
            new Pair<String, Runnable>("Continuer", () -> {
                //stage2.close();
                try{
                    enCours = new Level(-1);
                    window.setScene(new View(enCours));
                } catch (Exception e){
                    System.out.println("Niveau manquant");
                }
            }),
            new Pair<String, Runnable>("Choix du Niveau", () -> {System.out.println("Choisir le niveau"); menuLevelAnimation();}),
            new Pair<String, Runnable>("Réglages", () -> {System.out.println("Modifier les réglages du jeu"); menuSettingsAnimation();}),
            new Pair<String, Runnable>("Quitter le jeu", Platform::exit)
        );

    public ArrayList<Pair<String, Runnable>> levelData = new ArrayList<>();

    public Pane root = new Pane(); //Panneau sur lequel on va superposer tous les éléments
    public boolean lvlSelect = false;
    public boolean settingsSelect = false;
    public VBox menuBox = new VBox(); //Boite invisible qui contient les items du menu
    public GridPane LevelBox = new GridPane(); //Boite invisible qui contient les différents niveaux à séléctionner
    public GridPane settingsBox = new GridPane();
    public HBox titleBox = new HBox(); //Boite invisible qui contient le titre
    double lineX;
    double lineY;

    public MenuApplication(){
        super();
    }

    private Parent createContent() throws MalformedURLException {
        loadSettings();

        LevelBox.setHgap(25); //Cette ligne et la suivante décident de l'écart entre les "cases" de niveau dans le menu de séléction du niveau
        LevelBox.setVgap(20);

        settingsBox.setHgap(25);
        settingsBox.setVgap(20);


        //setButton();

        addBackground(); //Fonction qui choisit une image, la floute et l'ajoute en fond du menu principal

        addTitle();//Fonction qui ajoute le titre créé par MenuTitle.java

        addMenu(lineX + 5, lineY + 5); //Crée tous les items du menu et les ajoute au Pane parent (root)
        addLevelSelect(WIDTH * 2.0, HEIGHT/4.0, 3);
        setSettingsBox(-WIDTH * 2.0, HEIGHT/4.0);

        startAnimation(); //Crée les animations du menu

        return root;
    }

    private void addBackground() throws MalformedURLException {
        File img = new File("img/hello.jpeg");
        String url = img.toURI().toURL().toString();
        ImageView imageView = new ImageView(new Image(url));
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);
        imageView.setEffect(new GaussianBlur());
        root.getChildren().add(imageView);
    }

    private void addTitle() {
        MenuTitle title = new MenuTitle();
        title.setTranslateX(WIDTH / 2.0 - title.getTitleWidth()/2);
        title.setTranslateY(HEIGHT / 3.0);
        titleBox.getChildren().add(title);
        root.getChildren().add(titleBox);
    }

    private void startAnimation() {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1));
        st.setToY(1);
        st.setOnFinished(e -> {
            for (int i = 0; i < menuBox.getChildren().size(); i++) {
                Node n = menuBox.getChildren().get(i);

                TranslateTransition tt = new TranslateTransition(Duration.seconds(1 + i * 0.15), n);
                tt.setToX(0);
                tt.setOnFinished(e2 -> n.setClip(null));
                tt.play();
            }
        });
        st.play();
    }

    void loadSettings(){
        try {
            FileReader reader = new FileReader("settings.json");
            JSONParser jsonParser = new JSONParser();
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            WIDTH = Math.toIntExact((long) obj.get("WIDTH"));
            HEIGHT = Math.toIntExact((long) obj.get("HEIGHT"));

            MUSIQUE =  ((Number) obj.get("MUSIQUE")).doubleValue();
            SONS = ((Number) obj.get("SONS")).doubleValue();

            lineX = WIDTH / 2.0 - 100.0; //Ajoute le menu au centre
            lineY = HEIGHT / 3.0 + 50.0; //Ajoute le menu au centre

        }catch(Exception e){
            System.out.println(e);
        }
    }

    void saveSettings(){
        try{
            FileReader reader = new FileReader("settings.json");
            JSONParser jsonParser = new JSONParser();
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            obj.put("WIDTH",WIDTH);
            obj.put("HEIGHT",HEIGHT);
            obj.put("MUSIQUE",MUSIQUE);
            obj.put("SONS",SONS);

            FileWriter writer = new FileWriter("settings.json");
            writer.write(obj.toJSONString());
            writer.close();

        }catch(Exception e){
            System.out.println(e);
        }
    }

    private void menuLevelAnimation(){
        lvlSelect = true;
        ScaleTransition st = new ScaleTransition((Duration.seconds(1)));
        st.setToY(1);
        st.setOnFinished(e -> {
            TranslateTransition tt = new TranslateTransition((Duration.seconds(1.5)), menuBox);
            TranslateTransition tt2 = new TranslateTransition((Duration.seconds(1)), titleBox);
            TranslateTransition tt3 = new TranslateTransition((Duration.seconds(1)), LevelBox);
            tt.setToY(1200);

            tt2.setToY(-150);

            tt3.setToY(HEIGHT/4.0);
            tt3.setToX(WIDTH/4.0);

            tt.play();
            tt2.play();
            tt3.play();
        });
        st.play();
    }

    private void menuSettingsAnimation(){
        settingsBox.setTranslateY((HEIGHT - settingsBox.getHeight())/2);

        settingsSelect = true;
        ScaleTransition st = new ScaleTransition((Duration.seconds(1)));
        st.setToY(1);
        st.setOnFinished(e ->{
            TranslateTransition tt = new TranslateTransition((Duration.seconds(1.5)), menuBox);
            TranslateTransition tt2 = new TranslateTransition((Duration.seconds(1)), titleBox);
            TranslateTransition settingsTransition = new TranslateTransition((Duration.seconds(1)), settingsBox);

            tt.setToY(1200);
            tt2.setToY(-75);

            settingsTransition.setToY((HEIGHT - settingsBox.getHeight())/2);
            settingsTransition.setToX((WIDTH - settingsBox.getWidth())/1.8);

            tt.play();
            tt2.play();
            settingsTransition.play();
        });
        st.play();
    }

    private void reverseLevelAnimation(){
        lvlSelect = false;
        ScaleTransition st = new ScaleTransition((Duration.seconds(1)));
        st.setToY(1);
        st.setOnFinished(e -> {
            TranslateTransition tt = new TranslateTransition((Duration.seconds(1)), menuBox);
            TranslateTransition tt2 = new TranslateTransition((Duration.seconds(1)), titleBox);
            TranslateTransition tt3 = new TranslateTransition((Duration.seconds(1)), LevelBox);

            tt3.setToX(WIDTH * 2.0);


            tt2.setToY(tt2.getByY());

            tt.setToY(lineY + 5.0);
            tt.setToX(lineX + 5.0);

            tt.play();
            tt2.play();
            tt3.play();
        });
        st.play();
    }

    private void reverseSettingsAnimation(){
        settingsSelect = false;

        ScaleTransition st = new ScaleTransition((Duration.seconds(1)));
        st.setToY(1);
        st.setOnFinished(e->{
            TranslateTransition tt = new TranslateTransition((Duration.seconds(1)), menuBox);
            TranslateTransition tt2 = new TranslateTransition((Duration.seconds(1)), titleBox);
            TranslateTransition settingsTransition = new TranslateTransition((Duration.seconds(1)), settingsBox);

            settingsTransition.setToX(-WIDTH * 2.0);

            tt2.setToY(tt2.getByY());

            tt.setToY(lineY + 5.0);
            tt.setToX(lineX + 5.0);

            tt.play();
            tt2.play();
            settingsTransition.play();
        });
        st.play();
    }

    private void addMenu(double x, double y) {
        menuBox.setTranslateX(x);
        menuBox.setTranslateY(y);
        menuData.forEach(data -> {
            MenuItems item = new MenuItems(data.getKey());
            item.setOnAction(data.getValue());
            item.setTranslateX(-400);

            Rectangle clip = new Rectangle(300, 30);
            clip.translateXProperty().bind(item.translateXProperty().negate());

            item.setClip(clip);

            menuBox.getChildren().addAll(item);
        });

        root.getChildren().add(menuBox);
    }


    public void addLevelToList(List<Pair<String, Runnable>> list){
        for(int i = 0; i < lvls.length; i++){
            lvls[i] = lvls[i].split("\\.")[0];
            if(!lvls[i].equals("")) lvls[i] = lvls[i].substring(5);

            if(lvls[i] != null && !lvls[i].equals("-1")) {
                int finalI = i;
                list.add(new Pair<>(lvls[i], () -> {
                    try {
                        enCours = new Level(Integer.parseInt(lvls[finalI]));
                        window.setScene(new View(enCours));
                        //po.start(stage2);
                    } catch (Exception ex) {
                        System.out.println("Niveau manquant");
                    }
                }));
            }
        }
    }

    private void setSettingsBox(double x, double y){
        settingsBox.setTranslateX(x);
        settingsBox.setTranslateY(y);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(25);
        settingsBox.getColumnConstraints().addAll(col1,col2,col3,col4);


        Slider musique = new Slider(0,100,MUSIQUE);
        Label labelMusique = new Label("Musique :");
        Label niveauMusique = new Label(Integer.toString((int)musique.getValue()));
        settingsBox.add(labelMusique,0,0,1,1);
        settingsBox.add(musique,1,0,2,1);
        settingsBox.add(niveauMusique,3,0,1,1);

        Slider bruitages = new Slider(0,100,SONS);
        Label labelBruitages = new Label("Sons :");
        Label niveauBruitages = new Label(Integer.toString((int)bruitages.getValue()));
        settingsBox.add(labelBruitages,0,1,1,1);
        settingsBox.add(bruitages,1,1,2,1);
        settingsBox.add(niveauBruitages,3,1,1,1);

        musique.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                niveauMusique.setText(Integer.toString((int)musique.getValue()));
            }
        });

        bruitages.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                niveauBruitages.setText(Integer.toString((int)bruitages.getValue()));
            }
        });

        MenuItems retour = new MenuItems("Retour");
        retour.setOnAction(new Runnable(){
            @Override
            public void run() {
                reverseSettingsAnimation();
                musique.setValue(MUSIQUE);
                bruitages.setValue(SONS);
            }
        });
        MenuItems sauvegarder = new MenuItems("Sauvegarder les changements");
        sauvegarder.setOnAction(new Runnable(){
            @Override
            public void run(){
                MUSIQUE = musique.getValue();
                SONS = bruitages.getValue();
                saveSettings();
            }
        });

        settingsBox.add(retour,0,3,2,1);
        settingsBox.add(sauvegarder,2,3,2,1);

        root.getChildren().add(settingsBox);
    }

    private void addLevelSelect(double x, double y, int taillemax) {
        final int[] col = {0}; //Attribut colonne pour la création de la liste de niveaux visuelle
        final int[] ligne = {0};//Pareil mais pour les lignes
        LevelBox.setTranslateX(x);
        LevelBox.setTranslateY(y);
        addLevelToList(levelData);
        levelData.forEach(data -> {
            if (!data.getKey().equals("")){
                LevelItems l = new LevelItems(data.getKey());
                l.setOnAction(data.getValue());

                Rectangle clip = new Rectangle(200, 100);//Coupe le Polygon dans LvlItems, s'il est plus grand que 200x100
                clip.translateXProperty().bind(l.translateXProperty().negate());

                l.setClip(clip);

                LevelBox.add(l, col[0]%taillemax, ligne[0], 1, 1);// On ajoute le niveau à la colonne "colonne mod taillemax" et ligne
                if ((col[0]+ 1)%taillemax == 0) ligne[0] = ligne[0] + 1; //Si on arrive au bout de la ligne (nb max d'éléments par ligne) on pas à la suivante
                col[0] = col[0] + 1;//On passe à la colonne suivante
            }
        });
        MenuItems retour = new MenuItems("Retour");
        retour.setOnAction(new Runnable() {
            @Override
            public void run() {
                reverseLevelAnimation();
            }
        });
        LevelBox.add(retour,1,1,1,1);

        root.getChildren().add(LevelBox);
    }

    @Override
    public void start(Stage primaryStage) throws MalformedURLException {
            Scene scene = new Scene(createContent());
            window = primaryStage;
            primaryStage.setTitle("Aquavias");
            primaryStage.setScene(scene);
            primaryStage.setWidth(WIDTH);
            primaryStage.setHeight(HEIGHT);
            primaryStage.show();
            scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent keyEvent) {
                    if (lvlSelect){
                        if(keyEvent.getCode() == KeyCode.BACK_SPACE) reverseLevelAnimation();
                    }
                    if(settingsSelect){
                        if(keyEvent.getCode() == KeyCode.BACK_SPACE) reverseSettingsAnimation();
                    }
                }
            });
        }

    public static void main(String[] args) {
        launch(args);
    }
}
