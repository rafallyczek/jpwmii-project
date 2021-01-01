package cars.models;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Obstacle {
    
    private int x = -170;
    private final int y;
    private final int row;
    private int speed;
    private final int maxWidth = 1024;
    BufferedImage obstacleSprite;
    boolean showBounds = false;
    
    public Obstacle(BufferedImage obstacleSprite, int row, boolean showBounds, int speed){
        this.obstacleSprite = obstacleSprite;
        this.row = row;
        this.showBounds = showBounds;
        this.speed = speed;
        y = row*100;
    }
    
    public void action(){
        if(x<=maxWidth){
            x += speed;
        }
    }
    
    public void paintObject(Graphics graph){
        Graphics graphTemp = graph.create();
        Graphics2D graph2d = (Graphics2D)graphTemp;
        graph2d.drawImage(obstacleSprite, x, y, 160, 80, null);
        if(showBounds){
            graph2d.setColor(Color.red);
            graph2d.setStroke(new BasicStroke(1));
            graph2d.drawRect(x+15, y+5, 130, 70);
            graph2d.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));
            graph2d.drawString("x: "+(x+15)+" y: "+(y+5), x, y);
        }   
    }
    
    public Rectangle getBounds(){
        return new Rectangle(x+15,y+5,130,70);
    }

    public int getSpeed() {
        return speed;
    }
    
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    
    public void setShowBounds(boolean showBounds) {
        this.showBounds = showBounds;
    }
    
}
