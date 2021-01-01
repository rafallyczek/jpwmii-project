package cars.models;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class PlayerCar {
    
    private int x = 800;
    private int y = 300;
    private int speed = 10;
    private int keyFrame = 0;
    BufferedImage carSprite;
    BufferedImage carUp;
    BufferedImage carForward;
    BufferedImage carDown;
    BufferedImage explosionSprite;
    String usedSprite = "forward";
    boolean showBounds = false;
    boolean exploded = false;
    
    public PlayerCar(BufferedImage carSprite,BufferedImage explosionSprite){
        this.carSprite = carSprite;
        this.explosionSprite = explosionSprite;
        carUp = carSprite.getSubimage(0, 0, 160, 80);
        carForward = carSprite.getSubimage(160, 0, 160, 80);
        carDown = carSprite.getSubimage(320, 0, 160, 80);
    }
    
    public void action(KeyEvent ke){
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_UP:
                if(y>0){
                    y-=speed;
                }   break;
            case KeyEvent.VK_DOWN:
                if(y<600){
                    y+=speed;
                }   break;
            case KeyEvent.VK_LEFT:
                if(x>50){
                    x-=speed;
                }   break;
            case KeyEvent.VK_RIGHT:
                if(x<800){
                    x+=speed;
                }   break;
            default:
                break;
        }
    }
    
    public void paintObject(Graphics graph){
        Graphics graphTemp = graph.create();
        Graphics2D graph2d = (Graphics2D)graphTemp;
        switch (usedSprite) {
            case "forward":
                graph2d.drawImage(carForward, x, y, 160, 80, null);
                break;
            case "up":
                graph2d.drawImage(carUp, x, y, 160, 80, null);
                break;
            case "down":
                graph2d.drawImage(carDown, x, y, 160, 80, null);
                break;
            default:
                break;
        }
        if(showBounds){
            graph2d.setColor(Color.red);
            graph2d.setStroke(new BasicStroke(1));
            graph2d.drawRect(x+15, y+5, 130, 70);
            graph2d.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));
            graph2d.drawString("x: "+(x+15)+" y: "+(y+5), x, y);
        } 
        if(exploded){
            graph2d.drawImage(explosionSprite.getSubimage(keyFrame*64, 0, 64, 64), x, y-40, 160, 160, null);
            if(keyFrame<24){
                keyFrame++;
            }else{
                exploded = false;
                keyFrame = 0;
            }
        }
    }

    public Rectangle getBounds(){
        return new Rectangle(x+15,y+5,130,70);
    }
    
    public void setUsedSprite(String usedSprite) {
        this.usedSprite = usedSprite;
    }
    
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    
    public void setShowBounds(boolean showBounds) {
        this.showBounds = showBounds;
    }

    public void setExploded(boolean exploded) {
        this.exploded = exploded;
    }
    
}
