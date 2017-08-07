package helper;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Timeout implements ActionListener{

    private Timer timer;
    public boolean flag;


    public Timeout(int dalay){
        timer=new Timer(dalay,this);
        flag=true;
    }

    public void startUp(){
        this.timer.start();
    }

    public void showDown(){
        this.timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        flag=false;
    }
}
