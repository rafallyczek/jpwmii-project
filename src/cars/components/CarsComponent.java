package cars.components;

import cars.models.Obstacle;
import cars.models.PlayerCar;
import cars.models.RoadLine;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class CarsComponent extends JComponent implements ActionListener, KeyListener{
    
    //Linie na drodze i przeszkody
    List<RoadLine> roadLines = new ArrayList();
    List<Obstacle> obstacles = new ArrayList();
    
    //Tekstury wyglądu przeszkód
    List<BufferedImage> obstacleSprites = new ArrayList();
    
    //Timery: przesuwanie obiektów, odblokowywanie tablicy używanych wierszy, usuwanie przeszkód, tworzenie przeszkód, kończenie gry
    Timer gameTimer = new Timer(30,this);
    Timer clearArrayTimer = null;
    Timer clearObstacleTimer = null;
    Timer createObstacleTimer = null;
    Timer gameOverTimer = null;
    Timer speedTimer = null;
    
    //Obrazy przechowujące tekstury obiektów
    BufferedImage carSprite = null;
    BufferedImage carObstacles = null;
    BufferedImage explosionSprite = null;
    
    //Dźwięki
    AudioClip engine;
    AudioClip explosion;
    
    //Samochód gracza
    PlayerCar playerCar = null;
    Random generator = new Random();
    
    //Limit przeszkód tworzonych jednocześnie
    int rowLimit = 3;
    
    //Indeks w tablicy używanych wierszy
    int rowPoz = 0;
    
    //Wiersz przeszkody
    int row = generator.nextInt(7);
    
    //Tablica używanych wierszy
    int[] usedRows = {-1,-1,-1,-1,-1,-1,-1};
    
    //Metody wykonywane przez timery
    ActionListener clearArray = null;
    ActionListener clearObstacle = null;
    ActionListener createObstacle = null;
    ActionListener gameOverListener = null;
    ActionListener increaseSpeed = null;
    
    //Czas gry (wynik gracza)
    long startTime;
    long endTime;
    
    //Czy tryb info włączony
    boolean showBounds = false;
    
    //Czy gra skończona
    boolean gameOver = false;
    
    //Odtwarzacz muzyki
    MediaPlayer mediaPlayer;
    
    //Lista utworów
    List<Media> mediaList = new ArrayList();
    
    //Url utworów
    URL mp3ResourceURL;
    
    //Indeks tablicy utworów
    int index = 0;
    
    //Ilość utworów
    int musicCount;
    
    //Moment zapauzowania utworu
    Duration pauseTime;
    
    //Czy jest zapauzowany
    boolean isPaused = false;
    
    //Czy rozpoczęto odtwarzanie muzyki
    boolean isPlaying = false;
    
    //Głośność muzyki
    double volume = 0.3;
    
    //Czy gra wystartowała
    boolean gameStarted = false;
    
    //Prędkość przeszkód
    int obstacleSpeed = 6;
    
    public CarsComponent(){
        
        JFXPanel jfxPanel = new JFXPanel();
        this.add(jfxPanel);
        this.setSize(new Dimension(1024,700));
        this.setMaximumSize(new Dimension(1024,700));
        this.setMinimumSize(new Dimension(1024,700));
        
        //Obrazy
        try{
            carSprite = ImageIO.read(CarsComponent.class.getResource("/cars/resources/images/car-sprite.png"));
            carObstacles = ImageIO.read(CarsComponent.class.getResource("/cars/resources/images/car-obstacles.png"));
            explosionSprite = ImageIO.read(CarsComponent.class.getResource("/cars/resources/images/explosion.png"));
        }catch(Exception ex){
            System.err.println("Błąd wczytywania zasobu: "+ex.toString());
        }
        
        //Dodanie utworów do listy
        mp3ResourceURL = CarsComponent.class.getResource("/cars/resources/sound/bensound-happyrock.mp3");
        mediaList.add(new Media(mp3ResourceURL.toString()));
        mp3ResourceURL = CarsComponent.class.getResource("/cars/resources/sound/bensound-punky.mp3");
        mediaList.add(new Media(mp3ResourceURL.toString()));
        musicCount = mediaList.size()-1;
        
        //Dźwięki
        URL soundURL = CarsComponent.class.getResource("/cars/resources/sound/engine.wav");
        engine = new AudioClip(soundURL.toString());
        engine.setCycleCount(AudioClip.INDEFINITE);
        soundURL = CarsComponent.class.getResource("/cars/resources/sound/explosion.wav");
        explosion = new AudioClip(soundURL.toString());
        
        //Wypełnianie tablicy tekstur przeszkód
        for(int i=0;i<24;i++){
            obstacleSprites.add(carObstacles.getSubimage(0, i*80, 160, 80));
        }
        playerCar = new PlayerCar(carSprite,explosionSprite);
        
        //Wypełnianie tablicy linii na drodze
        for(int i=1;i<7;i++){
            for(int j=0;j<9;j++){
                roadLines.add(new RoadLine(j*128,i*100));
            }
        }
        
        //Odblokowywanie tablicy używanych wierszy
        clearArray = new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                for(int i : usedRows){
                    i = -1;
                }
                rowLimit = 3;
                rowPoz = 0;
            }
        };
        
        //Usuwanie przeszkód
        clearObstacle = new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                obstacles.remove(0);
            }
        };
        
        //Tworzenie przeszkód
        createObstacle = new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                if(rowLimit>0){
                    while(IntStream.of(usedRows).anyMatch(x -> x==row)){
                        row = generator.nextInt(7);
                    }
                    usedRows[rowPoz] = row;
                    obstacles.add(new Obstacle(obstacleSprites.get(generator.nextInt(obstacleSprites.size())),row,showBounds,obstacleSpeed));
                    rowPoz++;
                    rowLimit--;
                }
            }
        };
        
        //Kończenie gry
        gameOverListener = new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                gameOver();
            }
        };
        
        increaseSpeed = new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                if(obstacleSpeed<=10){
                    obstacleSpeed += 1;
                    obstacles.forEach((obstacle) -> {
                        obstacle.setSpeed(obstacleSpeed);
                    });
                }
                if(createObstacleTimer.getDelay()>200){
                    createObstacleTimer.setDelay(createObstacleTimer.getDelay()-100);
                }
                if(clearArrayTimer.getDelay()>1600){
                    clearArrayTimer.setDelay(clearArrayTimer.getDelay()-200);
                } 
            }
        };
        
        //Ustawianie i startowanie Timerów
        createObstacleTimer = new Timer(1000,createObstacle);
        createObstacleTimer.setInitialDelay(0);
        clearArrayTimer = new Timer(3000,clearArray);
        clearObstacleTimer = new Timer(1000,clearObstacle);
        clearObstacleTimer.setInitialDelay(7000);
        gameOverTimer = new Timer(1450,gameOverListener);
        gameOverTimer.setRepeats(false);
        //180000 = 3 min
        speedTimer = new Timer(180000,increaseSpeed);
        this.setFocusable(true);
        addKeyListener(this);
    }
    
    @Override
    public void paintComponent(Graphics graph){
        
        Graphics2D graph2d = (Graphics2D)graph;
        Dimension componentSize = getSize();
        
        //Tło
        graph2d.setPaint(new Color(102,102,102));
        graph2d.fillRect(0, 0, componentSize.width, componentSize.height);
        
        //Linie na drodze
        Iterator<RoadLine> roadLineIterator = roadLines.iterator();
        while(roadLineIterator.hasNext()){
            roadLineIterator.next().paintObject(graph);
        }
        
        //Przeszkody
        Iterator<Obstacle> obstacleIterator = obstacles.iterator();
        while(obstacleIterator.hasNext()){
            obstacleIterator.next().paintObject(graph);
        }
        
        //Samochód gracza
        playerCar.paintObject(graph);
        
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        
        //Linie na drodze
        Iterator<RoadLine> roadLineIterator = roadLines.iterator();
        while(roadLineIterator.hasNext()){
            RoadLine roadLine = roadLineIterator.next();
            if(!gameOver){
                roadLine.action();
            }                
        }
        
        //Przeszkody
        Iterator<Obstacle> obstacleIterator = obstacles.iterator();
        while(obstacleIterator.hasNext()){
            Obstacle obstacle = obstacleIterator.next();
            if(!gameOver){
                obstacle.action();
            }
            //Kolizje
            if(obstacle.getBounds().intersects(playerCar.getBounds())){
                if(!gameOver){
                    explosion.play(0.5);
                }
                gameOver = true;
                engine.stop();
                if(mediaPlayer!=null){
                    mediaPlayer.stop();
                }
                playerCar.setExploded(true);
                endTime = System.currentTimeMillis();
                gameOverTimer.start();
            }
        }
        this.repaint();  
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        //
    }

    //Animowanie samochodu gracza
    @Override
    public void keyPressed(KeyEvent ke) {
        playerCar.action(ke);
        if(ke.getKeyCode()==KeyEvent.VK_UP){
            playerCar.setUsedSprite("up");
        }else if(ke.getKeyCode()==KeyEvent.VK_DOWN){
            playerCar.setUsedSprite("down");
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        playerCar.setUsedSprite("forward");
    }
    
    //Rozpoczynanie gry
    public void gameStart(){
        if(!gameStarted){
            gameStarted = true;
            startTime = System.currentTimeMillis();
            gameTimer.start();
            createObstacleTimer.start();
            clearArrayTimer.start();
            clearObstacleTimer.start();
            speedTimer.start();
            engine.play(0.1);
        }
    }
    
    //Kończenie gry
    public void gameOver(){
        
        gameOver = true;
        
        //obliczenie wyniku gracza
        long milliseconds = endTime-startTime;
        long minutes = (milliseconds/1000)/60;
        long seconds = (milliseconds/1000)%60;
        String score = ((minutes<10)?"0"+minutes:minutes)+":"+((seconds<10)?"0"+seconds:seconds);
        
        //Zatrzymanie timerów
        createObstacleTimer.stop();
        clearArrayTimer.stop();
        clearObstacleTimer.stop();
        gameTimer.stop();
        speedTimer.stop();
        
        //Reset zmiennych
        obstacles.clear();
        for(int i : usedRows){
            i = -1;
        }
        rowLimit = 3;
        rowPoz = 0;
        isPlaying = false;
        isPaused = false;
        obstacleSpeed = 6;
        
        //Wyświetlenie podsumowania
        String[] options = {"Zagraj ponownie","Koniec"};
        int choice = JOptionPane.showOptionDialog(this, "Wynik: "+score,"Koniec gry",JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,null);
        
        //Ponowna gra lub zakończenie
        if(choice==JOptionPane.YES_OPTION){
            gameOver = false;
            playerCar = new PlayerCar(carSprite,explosionSprite);
            startTime = System.currentTimeMillis();
            showBounds = false;
            createObstacleTimer.setDelay(1000);
            clearArrayTimer.setDelay(3000);
            gameTimer.start();
            createObstacleTimer.start();
            clearArrayTimer.start();
            clearObstacleTimer.start();
            speedTimer.start();
            engine.play(0.1);
        }else{
            System.exit(0);
        }
    }
    
    //Startowanie odtwarzania muzyki
    public void playMusic(){
        if(!isPlaying){
            mediaPlayer = new MediaPlayer(mediaList.get(index));
            mediaPlayer.setVolume(volume);
            mediaPlayer.setOnEndOfMedia(new Runnable(){
                @Override
                public void run(){
                    nextTrack();
                }
            });
            mediaPlayer.play();
            isPlaying = true;
        }
        if(isPaused){
            mediaPlayer.seek(pauseTime);
            mediaPlayer.play();
            isPaused = false;
        }  
    }
    
    //Zatrzymanie odtwarzania muzyki
    public void stopMusic(){
        if(mediaPlayer!=null){
            mediaPlayer.pause();
            pauseTime = mediaPlayer.getCurrentTime();
            isPaused = true;
        }
    }
    
    //Zmiana głośności
    public void setVolume(double volume){
        if(mediaPlayer!=null){
            mediaPlayer.setVolume(volume);
            this.volume = volume;
        }
    }
    
    //Następny utwór
    public void nextTrack(){
        if(mediaPlayer!=null){
            if(isPlaying){
                mediaPlayer.stop();
                isPlaying = false;
                if(index<musicCount){
                    index++;
                }else{
                    index = 0;
                }
                playMusic();
            }
        }
    }
    
    //Poprzedni utwór
    public void previousTrack(){
        if(mediaPlayer!=null){
            if(isPlaying){
                mediaPlayer.stop();
                isPlaying = false;
                if(index>0){
                    index--;
                }else{
                    index = musicCount;
                }
                playMusic();
            }
        }
    }
    
    //Przełączanie trybu info
    public void switchShowBounds(){
        if(!showBounds){
            showBounds = true;
            playerCar.setShowBounds(true);
            obstacles.forEach((obstacle) -> {
                obstacle.setShowBounds(true);
            });
        }else{
            showBounds = false;
            playerCar.setShowBounds(false);
            obstacles.forEach((obstacle) -> {
                obstacle.setShowBounds(false);
            });
        }
    }

    public boolean isShowBounds() {
        return showBounds;
    }
    
}
