package cars.models;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class RoadLine {
    
    private int x;
    private final int y;
    private int speed = 4;
    private final int maxWidth = 1024;
    
    public RoadLine(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void action(){
        if(x<maxWidth){
            x += speed;
        }else{
            x = -128;
        }    
    }
    
    public void paintObject(Graphics graph){
        Graphics graphTemp = graph.create();
        Graphics2D graph2d = (Graphics2D)graphTemp;
        graph2d.setPaint(Color.WHITE);
        graph2d.setStroke(new BasicStroke(1));
        graph2d.fillRect(x,y,80,10);
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
      
}
