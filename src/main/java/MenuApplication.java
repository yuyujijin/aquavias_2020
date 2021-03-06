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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.scene.input.KeyEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.util.Arrays;

import static javafx.scene.media.MediaPlayer.INDEFINITE;


public class MenuApplication extends Application {

    private static int WIDTH;
    private static int HEIGHT;
    Level enCours;
    View v;
    File levelsFolder = new File("levels");
    String [] lvls = levelsFolder.list((dir,name) -> name.contains("level"));
    AnimationTimer at;
    Stage window;
    AudioController mediaPlayer = new AudioController();

    public List<Pair<String, Runnable>> menuData = Arrays.asList( //Définit une liste qui comprend tous les boutons sous un couple de String et d'action à effectuer
            //Bouton Nouvelle Partie du menu principal
            new Pair<String, Runnable>("Nouvelle Partie", () -> {
                try{
                    enCours = new Level(0);
                    fadeOut(enCours);
                } catch (Exception e){
                    System.out.println("Niveau manquant");
                }
            }),
            //Bouton continuer du menu principal
            new Pair<String, Runnable>("Continuer", () -> {
                try{
                    enCours = new Level(levelTracker.getLastPlayed());
                    fadeOut(enCours);
                } catch (Exception e){
                    System.out.println("Niveau manquant");
                }
            }),
            new Pair<String, Runnable>("Choix du Niveau", () -> {System.out.println("Choisir le niveau"); menuLevelAnimation();}),
            new Pair<String, Runnable>("Réglages", () -> {System.out.println("Modifier les réglages du jeu"); menuSettingsAnimation();}),
            new Pair<String, Runnable>("Quitter le jeu", Platform::exit)

    );

    public ImageView retour;
    public ArrayList<Pair<String, Runnable>> levelData = new ArrayList<>();

    public Pane root = new Pane(); //Panneau sur lequel on va superposer tous les éléments
    public boolean lvlSelect = false;
    public boolean settingsSelect = false;
    public VBox menuBox = new VBox(); //Boite invisible qui contient les items du menu
    public GridPane LevelBox = new GridPane(); //Boite invisible qui contient les différents niveaux à séléctionner
    public settingsMenu settingsBox;
    public HBox titleBox = new HBox(); //Boite invisible qui contient le titre
    double lineX;
    double lineY;
    public static levelTracker levelTracker;

    public MenuApplication(){
        super();
    }

    public MenuApplication(int i){
        super();
        launch();
    }

    private Parent createContent() throws MalformedURLException {
        loadSettings();

        levelTracker = new levelTracker();

        MenuItems.setAudioController(mediaPlayer);

        settingsBox = new settingsMenu(mediaPlayer, Color.BLACK);

        LevelBox.setHgap(25); //Cette ligne et la suivante décident de l'écart entre les "cases" de niveau dans le menu de séléction du niveau
        LevelBox.setVgap(20);

        addBackground(); //Fonction qui choisit une image, la floute et l'ajoute en fond du menu principal

        retour = new ImageView(new Image(new File("img/retour.png").toURI().toString()));

        retour.setTranslateX((WIDTH/5.0));
        retour.setTranslateY(HEIGHT/1.2);


        addTitle();//Fonction qui ajoute le titre créé par MenuTitle.java


        addMenu(lineX + 5, lineY + 5); //Crée tous les items du menu et les ajoute au Pane parent (root)
        addLevelSelect(WIDTH*2, HEIGHT/4.0, 5);
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

    void playSon(String s){
        mediaPlayer.play(s);
    }

    void loadSettings(){
        WIDTH = 1280;
        HEIGHT = 720;

        lineX = WIDTH / 2.0 - 100.0; //Ajoute le menu au centre
        lineY = HEIGHT / 3.0 + 50.0; //Ajoute le menu au centre
    }

    private void menuLevelAnimation(){
        lvlSelect = true;
        root.getChildren().add(retour);
        labelAnimation();
        ScaleTransition st = new ScaleTransition((Duration.seconds(1)));
        st.setToY(1);
        st.setOnFinished(e -> {
            TranslateTransition tt = new TranslateTransition((Duration.seconds(1.5)), menuBox);
            TranslateTransition tt2 = new TranslateTransition((Duration.seconds(1)), titleBox);
            TranslateTransition tt3 = new TranslateTransition((Duration.seconds(1)), LevelBox);
            tt.setToY(1200);

            tt2.setToY(-150);

            tt3.setToY(HEIGHT/4.0);
            tt3.setToX(WIDTH/4.0-220);

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
        if(at != null) at.stop();
        root.getChildren().remove(retour);
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

    public void labelAnimation(){ //Cette fonction permet l'animation du texte lorsque l'écran est celui d'une séléction
        if (at == null) at = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (retour.getOpacity() - 0.01 > 0.1) {
                    retour.setOpacity(retour.getOpacity() - 0.01);
                } else
                    retour.setOpacity(retour.getOpacity() + 1.0);
            }
        };
        at.start();
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
        /* tri des niveaux pour les avoir dans l'ordre croissant */

        Arrays.sort(lvls, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2){
                int i1 = Integer.valueOf(o1.substring(5,o1.indexOf('.')));
                int i2 = Integer.valueOf(o2.substring(5,o2.indexOf('.')));
                return i1 - i2;
            }
        });
        for(int i = 0; i < lvls.length; i++){
            lvls[i] = lvls[i].split("\\.")[0];
            if(!lvls[i].equals("")) lvls[i] = lvls[i].substring(5);
            if(lvls[i] != null && !lvls[i].equals("-1")) {
                int finalI = i;
                list.add(new Pair<>(lvls[i], () -> {
                    try {
                        enCours = new Level(Integer.parseInt(lvls[finalI]));

                        /* on ajoute le son aussi */
                        mediaPlayer.play("click");

                        fadeOut(enCours);
                    } catch (Exception ex) {
                        System.out.println("Niveau manquant");
                    }
                }));
            }
        }
    }

    AudioController getMediaPlayer(){ return mediaPlayer; }

    private void setSettingsBox(double x, double y){
        /* place le menu de settings a gauche */
        settingsBox.setTranslateX(x);
        settingsBox.setTranslateY(y);

        /* on créer un runnable pour effectuer l'animation inverse */
        Runnable r = new Runnable(){
            @Override
            public void run() {
                reverseSettingsAnimation();

                /* pour reset les valeurs si non sauvegardé */
                settingsBox.updateValues();
            }
        };

        /* et on l'applique comme action de retour a notre bouton */
        settingsBox.setRetourAction(r);

        root.getChildren().add(settingsBox);
    }

    private void addLevelSelect(double x, double y, int taillemax) {
        final int[] col = {0}; //Attribut colonne pour la création de la liste de niveaux visuelle
        final int[] ligne = {0};//Pareil mais pour les lignes
        LevelBox.setTranslateX(x);
        LevelBox.setTranslateY(y);
        addLevelToList(levelData);

        final int[] i = {0};

        levelData.forEach(data -> {
            if (!data.getKey().equals("")){
                LevelItems l = new LevelItems(data.getKey());
                l.setOnAction(data.getValue());

                //si le niveau est débloque alors on l'affiche comme tel
                if(i[0] <= levelTracker.getMaxLevel()) l.setUnlocked(true);


                Rectangle clip = new Rectangle(200, 100);//Coupe le Polygon dans LvlItems, s'il est plus grand que 200x100
                clip.translateXProperty().bind(l.translateXProperty().negate());

                l.setClip(clip);

                LevelBox.add(l, col[0]%taillemax, ligne[0], 1, 1);// On ajoute le niveau à la colonne "colonne mod taillemax" et ligne
                if ((col[0]+ 1)%taillemax == 0) ligne[0] = ligne[0] + 1; //Si on arrive au bout de la ligne (nb max d'éléments par ligne) on pas à la suivante
                col[0] = col[0] + 1;//On passe à la colonne suivante

                i[0]++;

            }
        });
        root.getChildren().add(LevelBox);
    }

    void fadeOut(Level lvl) throws Exception {
        View v = new View(lvl, this);

        mediaPlayer.pauseMusique();
        mediaPlayer.play("wind");

        /* on met a jour le dernier niveau joué */
        updateLastPlayed(lvl.ID);

        FadeTransition fade = new FadeTransition();
        fade.setDuration(Duration.millis(1000));
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setNode(root);
        fade.setOnFinished(EventHandler -> {
            window.setScene(v);
            reverseLevelAnimation();
            v.fadeIn();
        });
        fade.play();
    }

    void fadeIn(){
        window.setScene(primaryScene);

        FadeTransition fade = new FadeTransition();
        fade.setDuration(Duration.millis(1000));
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setNode(root);
        fade.setOnFinished(e -> {
            settingsBox.loadSettings();
            settingsBox.updateValues();
            mediaPlayer.playMusique();
        });
        fade.play();
    }

    void playMusique(){
        mediaPlayer.playMusique();
    }

    void pauseMusique(){
        mediaPlayer.pauseMusique();
    }

    int getMaxLevel(){
        return levelTracker.getMaxLevel();
    }

    void incrementeMax(){
        /* si on a pas dépassé les nombres de niveau dispo, on incrémente le niveau sur lequel on se trouve */
        if(levelTracker.getMaxLevel() < levelData.size() - 1) levelTracker.incrementeMax();

        /* puis on l'unlock */
        ((LevelItems) LevelBox.getChildren().get(levelTracker.getMaxLevel())).setUnlocked(true);
    }

    void unlockAll(){
        while(levelTracker.getMaxLevel() < levelData.size() - 1){
            levelTracker.incrementeMax();
            ((LevelItems) LevelBox.getChildren().get(levelTracker.getMaxLevel())).setUnlocked(true);
        }
    }

    void updateLastPlayed(int i){
        /* pour être sur de ne pas mettre un niveau joué a un niveau non existant */
        if(i < levelData.size()) levelTracker.setLastPlayed(i);
    }

    void nextLevel(int levelId) throws Exception {
        /* si on débloque le niveau, on incrémente le max */
        if(levelId + 1 > levelTracker.getMaxLevel()) incrementeMax();

        /* puis on lance l'animation pour charger le niveau */
        fadeOut(new Level(levelId + 1));
    }

    Scene primaryScene;

    @Override
    public void start(Stage primaryStage) throws MalformedURLException {


        Scene scene = new Scene(createContent());
        primaryScene = scene;
        window = primaryStage;
        primaryStage.setTitle("Aquavias");
        primaryStage.setScene(scene);
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
        primaryStage.show();

        cheatHandler c = new cheatHandler(10, this);

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                c.addInput(keyEvent);
                if (lvlSelect){
                    if(keyEvent.getCode() == KeyCode.BACK_SPACE) reverseLevelAnimation();
                }
                if(settingsSelect){
                    if(keyEvent.getCode() == KeyCode.BACK_SPACE) reverseSettingsAnimation();
                }
            }
        });
    }
}
