/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acousticfield3d.gui.misc;

import acousticfield3d.gui.MainForm;
import acousticfield3d.math.Vector3f;
import acousticfield3d.scene.Entity;
import acousticfield3d.simulation.Transducer;
import acousticfield3d.utils.Parse;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author am14010
 */
public class UDPRemoteControl extends javax.swing.JFrame {
    final MainForm mf;
    private Thread t = null;
    
    public UDPRemoteControl(MainForm mf) {
        this.mf = mf;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        portText = new javax.swing.JTextField();
        openButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        jLabel1.setText("Port:");

        portText.setText("8085");

        openButton.setText("Open");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(portText, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(portText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openButton)
                    .addComponent(closeButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closePort(){
        if (t != null){
            t.interrupt();
            t = null;
        }
    } 
    
    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        closePort();
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final DatagramSocket serverSocket = new DatagramSocket( Parse.toInt(portText.getText()));
                    final byte[] receiveData = new byte[1024];
                    final List<Transducer> trans = mf.simulation.transducers;
                    final int size = trans.size();
                    
                    while(! Thread.interrupted()) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        String sentence = new String( receivePacket.getData() ,
                                receivePacket.getOffset(), receivePacket.getLength());
                        
                        String[] s = sentence.split(" ");
                        final String command = s[0].toLowerCase();
                        if (command.startsWith("clear")){ //delete all the control points
                            mf.pointsPanel.deleteAllPoints();
                        }else if (command.startsWith("create")){ //create n points
                            if (s.length >= 2){
                                final int n = Parse.toInt( s[1] );
                                mf.pointsPanel.createPoints( n );
                            }
                        }else if (command.startsWith("place")){ //place a point at the specified position
                            if (s.length >= 5){
                                final int n = Parse.toInt( s[1] );
                                final Vector3f pos = new Vector3f(s[2], s[3], s[4]);
                                if (n >= 0 && n < mf.simulation.controlPoints.size()){
                                    final Entity e = mf.simulation.controlPoints.get(n);
                                    e.getTransform().getTranslation().set(pos);
                                }
                            }
                        }else if (command.startsWith("commit")){ //calculate and send
                            mf.algForm.runBFGS(false, false, true);
                            mf.transControlPanel.sendPattern();
                        }else if (command.startsWith("delete")){ //delete the Nth particle
                            if (s.length >= 2){
                                final int n = Parse.toInt( s[1] );
                                if (n >= 0 && n < mf.simulation.controlPoints.size()){
                                    mf.scene.getEntities().remove( mf.simulation.controlPoints.get(n) );
                                    mf.simulation.controlPoints.remove(n);
                                }
                            }
                        }else if (command.startsWith("update")){ //update the graphics
                            mf.needUpdate();
                        }else{
                            //send ERR
                        }
                    }
                    
                    serverSocket.close();
                } catch (SocketException ex) {
                    Logger.getLogger(UDPRemoteControl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UDPRemoteControl.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        t.start();
    }//GEN-LAST:event_openButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
       closePort();
       dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton openButton;
    private javax.swing.JTextField portText;
    // End of variables declaration//GEN-END:variables
}
