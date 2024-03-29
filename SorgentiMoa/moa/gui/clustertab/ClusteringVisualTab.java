/*
 *    ClusteringVisualTab.java
 *    Copyright (C) 2010 RWTH Aachen University, Germany
 *    @author Jansen (moa@cs.rwth-aachen.de)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */

package moa.gui.clustertab;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.ToolTipManager;
import moa.gui.FileExtensionFilter;
import moa.gui.visualization.GraphCanvas;
import moa.gui.visualization.RunVisualizer;
import moa.gui.visualization.StreamPanel;

public class ClusteringVisualTab extends javax.swing.JPanel implements ActionListener{
	private RunVisualizer visualizer = null;
    private Thread visualizerThread = null;
    private Boolean running = false;
    private ClusteringSetupTab clusteringSetupTab = null;
    private String exportFile;
    private String screenshotFilebase;

    /** Creates new form ClusteringVisualTab */
    public ClusteringVisualTab() {
        resetComponents();
    }

    private void resetComponents(){
        initComponents();
        comboY.setSelectedIndex(1);
        graphCanvas.setViewport(graphScrollPanel.getViewport());

        //TODO this needs to only affect the visual Panel
        ToolTipManager.sharedInstance().setDismissDelay(20000);
        ToolTipManager.sharedInstance().setInitialDelay(100);
    }

    public void setClusteringSetupTab(ClusteringSetupTab clusteringSetupTab){
        this.clusteringSetupTab = clusteringSetupTab;
    }


    private void createVisualiterThread(){
        visualizer = new RunVisualizer(this, clusteringSetupTab);
        visualizerThread = new Thread(visualizer);
    }

    public void setDimensionComobBoxes(int numDimensions){
        String[] dimensions = new String[numDimensions];
        for (int i = 0; i < dimensions.length; i++) {
            dimensions[i] = "Dim "+(i+1);

        }
        comboX.setModel(new javax.swing.DefaultComboBoxModel(dimensions));
        comboY.setModel(new javax.swing.DefaultComboBoxModel(dimensions));
        comboY.setSelectedIndex(1);
    }

    public StreamPanel getLeftStreamPanel(){
        return streamPanel0;
    }

    public StreamPanel getRightStreamPanel(){
        return streamPanel1;
    }

    public GraphCanvas getGraphCanvas(){
        return graphCanvas;
    }

    public ClusteringVisualEvalPanel getEvalPanel(){
        return clusteringVisualEvalPanel1;
    }

    public boolean isEnabledDrawPoints(){
        return checkboxDrawPoints.isSelected();
    }

    public boolean isEnabledDrawGroundTruth(){
        return checkboxDrawGT.isSelected();
    }

    public boolean isEnabledDrawMicroclustering(){
        return checkboxDrawMicro.isSelected();
    }
    public boolean isEnabledDrawClustering(){
        return checkboxDrawClustering.isSelected();
    }

    public void setProcessedPointsCounter(int value){
        label_processed_points_value.setText(Integer.toString(value));
    }

    public int getPauseInterval(){
        return Integer.parseInt(numPauseAfterPoints.getText());
    }

    public void setPauseInterval(int pause){
        numPauseAfterPoints.setText(Integer.toString(pause));
    }

    @Override
    public void repaint() {
        if(splitVisual!=null)
            splitVisual.setDividerLocation(splitVisual.getWidth()/2);
        super.repaint();
    }

    public void toggleVisualizer(boolean internal){
        if(visualizer == null)
            createVisualiterThread();

        if(!visualizerThread.isAlive()){
            visualizerThread.start();

        }
        //pause
        if(running){
            running = false;
            visualizer.pause();
            buttonRun.setText("Resume");
        }
        else{
            running = true;
            visualizer.resume();
            buttonRun.setText("Pause");
        }
        if(internal)
            clusteringSetupTab.toggleRunMode();
    }

    public void stopVisualizer(){
        visualizer.stop();
        running = false;
        visualizer = null;
        visualizerThread = null;
        removeAll();
        resetComponents();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        topWrapper = new javax.swing.JPanel();
        panelVisualWrapper = new javax.swing.JPanel();
        splitVisual = new javax.swing.JSplitPane();
        scrollPane1 = new javax.swing.JScrollPane();
        streamPanel1 = new moa.gui.visualization.StreamPanel();
        scrollPane0 = new javax.swing.JScrollPane();
        streamPanel0 = new moa.gui.visualization.StreamPanel();
        panelControl = new javax.swing.JPanel();
        buttonRun = new javax.swing.JButton();
        buttonStop = new javax.swing.JButton();
        buttonScreenshot = new javax.swing.JButton();
        speedSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        comboX = new javax.swing.JComboBox();
        labelX = new javax.swing.JLabel();
        comboY = new javax.swing.JComboBox();
        labelY = new javax.swing.JLabel();
        checkboxDrawPoints = new javax.swing.JCheckBox();
        checkboxDrawGT = new javax.swing.JCheckBox();
        checkboxDrawMicro = new javax.swing.JCheckBox();
        checkboxDrawClustering = new javax.swing.JCheckBox();
        label_processed_points = new javax.swing.JLabel();
        label_processed_points_value = new javax.swing.JLabel();
        labelNumPause = new javax.swing.JLabel();
        numPauseAfterPoints = new javax.swing.JTextField();
        panelEvalOutput = new javax.swing.JPanel();
        clusteringVisualEvalPanel1 = new moa.gui.clustertab.ClusteringVisualEvalPanel();
        graphPanel = new javax.swing.JPanel();
        graphPanelControlTop = new javax.swing.JPanel();
        buttonZoomInY = new javax.swing.JButton();
        buttonZoomOutY = new javax.swing.JButton();
        labelEvents = new javax.swing.JLabel();
        graphScrollPanel = new javax.swing.JScrollPane();
        graphCanvas = new moa.gui.visualization.GraphCanvas();
        graphPanelControlBottom = new javax.swing.JPanel();
        buttonZoomInX = new javax.swing.JButton();
        buttonZoomOutX = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jSplitPane1.setDividerLocation(400);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        topWrapper.setPreferredSize(new java.awt.Dimension(688, 500));
        topWrapper.setLayout(new java.awt.GridBagLayout());

        panelVisualWrapper.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelVisualWrapper.setLayout(new java.awt.BorderLayout());

        splitVisual.setDividerLocation(403);
        splitVisual.setResizeWeight(1.0);

        streamPanel1.setPreferredSize(new java.awt.Dimension(400, 250));

        javax.swing.GroupLayout streamPanel1Layout = new javax.swing.GroupLayout(streamPanel1);
        streamPanel1.setLayout(streamPanel1Layout);
        streamPanel1Layout.setHorizontalGroup(
            streamPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 428, Short.MAX_VALUE)
        );
        streamPanel1Layout.setVerticalGroup(
            streamPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 339, Short.MAX_VALUE)
        );

        scrollPane1.setViewportView(streamPanel1);

        splitVisual.setRightComponent(scrollPane1);

        scrollPane0.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                scrollPane0MouseWheelMoved(evt);
            }
        });

        streamPanel0.setPreferredSize(new java.awt.Dimension(400, 250));

        javax.swing.GroupLayout streamPanel0Layout = new javax.swing.GroupLayout(streamPanel0);
        streamPanel0.setLayout(streamPanel0Layout);
        streamPanel0Layout.setHorizontalGroup(
            streamPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        streamPanel0Layout.setVerticalGroup(
            streamPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 339, Short.MAX_VALUE)
        );

        scrollPane0.setViewportView(streamPanel0);

        splitVisual.setLeftComponent(scrollPane0);

        panelVisualWrapper.add(splitVisual, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.ipady = 200;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        topWrapper.add(panelVisualWrapper, gridBagConstraints);

        panelControl.setMinimumSize(new java.awt.Dimension(600, 52));
        panelControl.setPreferredSize(new java.awt.Dimension(600, 52));
        panelControl.setLayout(new java.awt.GridBagLayout());

        buttonRun.setText("Start");
        buttonRun.setPreferredSize(new java.awt.Dimension(90, 23));
        buttonRun.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buttonRunMouseClicked(evt);
            }
        });
        buttonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRunActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 1, 5);
        panelControl.add(buttonRun, gridBagConstraints);

        buttonStop.setText("Stop");
        buttonStop.setPreferredSize(new java.awt.Dimension(90, 23));
        buttonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStopActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        panelControl.add(buttonStop, gridBagConstraints);

        buttonScreenshot.setText("Screenshot");
        buttonScreenshot.setPreferredSize(new java.awt.Dimension(90, 23));
        buttonScreenshot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buttonScreenshotMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 1, 5);
        panelControl.add(buttonScreenshot, gridBagConstraints);

        speedSlider.setValue(100);
        speedSlider.setBorder(javax.swing.BorderFactory.createTitledBorder("Visualisation Speed"));
        speedSlider.setPreferredSize(new java.awt.Dimension(120, 48));
        speedSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                speedSliderMouseDragged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 1, 5);
        panelControl.add(speedSlider, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panelControl.add(jLabel1, gridBagConstraints);

        comboX.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Dim 1", "Dim 2", "Dim 3", "Dim 4" }));
        comboX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboXActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        panelControl.add(comboX, gridBagConstraints);

        labelX.setText("X");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 14, 0, 5);
        panelControl.add(labelX, gridBagConstraints);

        comboY.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Dim 1", "Dim 2", "Dim 3", "Dim 4" }));
        comboY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboYActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        panelControl.add(comboY, gridBagConstraints);

        labelY.setText("Y");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 14, 0, 5);
        panelControl.add(labelY, gridBagConstraints);

        checkboxDrawPoints.setSelected(true);
        checkboxDrawPoints.setText("Points");
        checkboxDrawPoints.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkboxDrawPoints.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxDrawPointsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 4);
        panelControl.add(checkboxDrawPoints, gridBagConstraints);

        checkboxDrawGT.setSelected(true);
        checkboxDrawGT.setText("Ground truth");
        checkboxDrawGT.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkboxDrawGT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxDrawGTActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        panelControl.add(checkboxDrawGT, gridBagConstraints);

        checkboxDrawMicro.setSelected(true);
        checkboxDrawMicro.setText("Microclustering");
        checkboxDrawMicro.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkboxDrawMicro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxDrawMicroActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 4);
        panelControl.add(checkboxDrawMicro, gridBagConstraints);

        checkboxDrawClustering.setSelected(true);
        checkboxDrawClustering.setText("Clustering");
        checkboxDrawClustering.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkboxDrawClustering.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxDrawClusteringActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        panelControl.add(checkboxDrawClustering, gridBagConstraints);

        label_processed_points.setText("Processed:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        panelControl.add(label_processed_points, gridBagConstraints);

        label_processed_points_value.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        panelControl.add(label_processed_points_value, gridBagConstraints);

        labelNumPause.setText("Pause in:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        panelControl.add(labelNumPause, gridBagConstraints);

        numPauseAfterPoints.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        numPauseAfterPoints.setText(Integer.toString(RunVisualizer.initialPauseInterval));
        numPauseAfterPoints.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        panelControl.add(numPauseAfterPoints, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        topWrapper.add(panelControl, gridBagConstraints);

        jSplitPane1.setLeftComponent(topWrapper);

        panelEvalOutput.setBorder(javax.swing.BorderFactory.createTitledBorder("Evaluation"));
        panelEvalOutput.setLayout(new java.awt.GridBagLayout());

        clusteringVisualEvalPanel1.setMinimumSize(new java.awt.Dimension(280, 118));
        clusteringVisualEvalPanel1.setPreferredSize(new java.awt.Dimension(290, 115));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        panelEvalOutput.add(clusteringVisualEvalPanel1, gridBagConstraints);

        graphPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Plot"));
        graphPanel.setPreferredSize(new java.awt.Dimension(530, 115));
        graphPanel.setLayout(new java.awt.GridBagLayout());

        graphPanelControlTop.setLayout(new java.awt.GridBagLayout());

        buttonZoomInY.setText("Zoom in Y");
        buttonZoomInY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonZoomInYActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        graphPanelControlTop.add(buttonZoomInY, gridBagConstraints);

        buttonZoomOutY.setText("Zoom out Y");
        buttonZoomOutY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonZoomOutYActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        graphPanelControlTop.add(buttonZoomOutY, gridBagConstraints);

        labelEvents.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        graphPanelControlTop.add(labelEvents, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        graphPanel.add(graphPanelControlTop, gridBagConstraints);

        graphCanvas.setPreferredSize(new java.awt.Dimension(500, 111));

        javax.swing.GroupLayout graphCanvasLayout = new javax.swing.GroupLayout(graphCanvas);
        graphCanvas.setLayout(graphCanvasLayout);
        graphCanvasLayout.setHorizontalGroup(
            graphCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 515, Short.MAX_VALUE)
        );
        graphCanvasLayout.setVerticalGroup(
            graphCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 128, Short.MAX_VALUE)
        );

        graphScrollPanel.setViewportView(graphCanvas);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        graphPanel.add(graphScrollPanel, gridBagConstraints);

        buttonZoomInX.setText("Zoom in X");
        buttonZoomInX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonZoomInXActionPerformed(evt);
            }
        });
        graphPanelControlBottom.add(buttonZoomInX);

        buttonZoomOutX.setText("Zoom out X");
        buttonZoomOutX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonZoomOutXActionPerformed(evt);
            }
        });
        graphPanelControlBottom.add(buttonZoomOutX);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        graphPanel.add(graphPanelControlBottom, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 1.0;
        panelEvalOutput.add(graphPanel, gridBagConstraints);

        jSplitPane1.setRightComponent(panelEvalOutput);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonScreenshotMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonScreenshotMouseClicked
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(true);
        if(screenshotFilebase!=null)
            fileChooser.setSelectedFile(new File(screenshotFilebase));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        	screenshotFilebase = fileChooser.getSelectedFile().getPath();
        	streamPanel0.screenshot(screenshotFilebase+"_"+label_processed_points_value.getText()+"_0", true, true);
            streamPanel1.screenshot(screenshotFilebase+"_"+label_processed_points_value.getText()+"_1", true, true);
        }

    }//GEN-LAST:event_buttonScreenshotMouseClicked

    private void buttonRunMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonRunMouseClicked
        toggleVisualizer(true);
    }//GEN-LAST:event_buttonRunMouseClicked

    private void speedSliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speedSliderMouseDragged
        visualizer.setSpeed((int)(speedSlider.getValue()/(100.0/15.0)));

    }//GEN-LAST:event_speedSliderMouseDragged

    private void scrollPane0MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollPane0MouseWheelMoved
        streamPanel0.setZoom(evt.getX(),evt.getY(),(-1)*evt.getWheelRotation(),scrollPane0);
    }//GEN-LAST:event_scrollPane0MouseWheelMoved

    private void buttonZoomInXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonZoomInXActionPerformed
        graphCanvas.scaleXResolution(false);
    }//GEN-LAST:event_buttonZoomInXActionPerformed

    private void buttonZoomOutYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonZoomOutYActionPerformed
        graphCanvas.setSize(new Dimension(graphCanvas.getWidth(), (int)(graphCanvas.getHeight()*0.8)));
        graphCanvas.setPreferredSize(new Dimension(graphCanvas.getWidth(), (int)(graphCanvas.getHeight()*0.8)));
    }//GEN-LAST:event_buttonZoomOutYActionPerformed

    private void buttonZoomOutXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonZoomOutXActionPerformed
        graphCanvas.scaleXResolution(true);
    }//GEN-LAST:event_buttonZoomOutXActionPerformed

    private void buttonZoomInYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonZoomInYActionPerformed
        graphCanvas.setSize(new Dimension(graphCanvas.getWidth(), (int)(graphCanvas.getHeight()*1.2)));
        graphCanvas.setPreferredSize(new Dimension(graphCanvas.getWidth(), (int)(graphCanvas.getHeight()*1.2)));
    }//GEN-LAST:event_buttonZoomInYActionPerformed

    private void checkboxDrawPointsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxDrawPointsActionPerformed
        visualizer.setPointLayerVisibility(checkboxDrawPoints.isSelected());
    }//GEN-LAST:event_checkboxDrawPointsActionPerformed

    private void checkboxDrawMicroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxDrawMicroActionPerformed
        //visualizer.redrawClusterings();
        visualizer.setMicroLayerVisibility(checkboxDrawMicro.isSelected());
    }//GEN-LAST:event_checkboxDrawMicroActionPerformed

    private void checkboxDrawGTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxDrawGTActionPerformed
        //visualizer.redrawClusterings();
        visualizer.setGroundTruthVisibility(checkboxDrawGT.isSelected());
    }//GEN-LAST:event_checkboxDrawGTActionPerformed

    private void checkboxDrawClusteringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxDrawClusteringActionPerformed
        //visualizer.redrawClusterings();
        visualizer.setMacroVisibility(checkboxDrawClustering.isSelected());
    }//GEN-LAST:event_checkboxDrawClusteringActionPerformed

    private void comboXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboXActionPerformed
        JComboBox cb = (JComboBox)evt.getSource();
        int dim = cb.getSelectedIndex();
        streamPanel0.setActiveXDim(dim);
        streamPanel1.setActiveXDim(dim);
        if(visualizer!=null)
            visualizer.redraw();
    }//GEN-LAST:event_comboXActionPerformed

    private void comboYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboYActionPerformed
        JComboBox cb = (JComboBox)evt.getSource();
        int dim = cb.getSelectedIndex();
        streamPanel0.setActiveYDim(dim);
        streamPanel1.setActiveYDim(dim);
        if(visualizer!=null)
            visualizer.redraw();
    }//GEN-LAST:event_comboYActionPerformed

    private void buttonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStopActionPerformed
        stopVisualizer();
        clusteringSetupTab.stopRun();
    }//GEN-LAST:event_buttonStopActionPerformed

    private void buttonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRunActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonRunActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonRun;
    private javax.swing.JButton buttonScreenshot;
    private javax.swing.JButton buttonStop;
    private javax.swing.JButton buttonZoomInX;
    private javax.swing.JButton buttonZoomInY;
    private javax.swing.JButton buttonZoomOutX;
    private javax.swing.JButton buttonZoomOutY;
    private javax.swing.JCheckBox checkboxDrawClustering;
    private javax.swing.JCheckBox checkboxDrawGT;
    private javax.swing.JCheckBox checkboxDrawMicro;
    private javax.swing.JCheckBox checkboxDrawPoints;
    private moa.gui.clustertab.ClusteringVisualEvalPanel clusteringVisualEvalPanel1;
    private javax.swing.JComboBox comboX;
    private javax.swing.JComboBox comboY;
    private moa.gui.visualization.GraphCanvas graphCanvas;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JPanel graphPanelControlBottom;
    private javax.swing.JPanel graphPanelControlTop;
    private javax.swing.JScrollPane graphScrollPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel labelEvents;
    private javax.swing.JLabel labelNumPause;
    private javax.swing.JLabel labelX;
    private javax.swing.JLabel labelY;
    private javax.swing.JLabel label_processed_points;
    private javax.swing.JLabel label_processed_points_value;
    private javax.swing.JTextField numPauseAfterPoints;
    private javax.swing.JPanel panelControl;
    private javax.swing.JPanel panelEvalOutput;
    private javax.swing.JPanel panelVisualWrapper;
    private javax.swing.JScrollPane scrollPane0;
    private javax.swing.JScrollPane scrollPane1;
    private javax.swing.JSlider speedSlider;
    private javax.swing.JSplitPane splitVisual;
    private moa.gui.visualization.StreamPanel streamPanel0;
    private moa.gui.visualization.StreamPanel streamPanel1;
    private javax.swing.JPanel topWrapper;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof JButton){
            if(e.getActionCommand().equals("csv export")){
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(true);
                fileChooser.addChoosableFileFilter(new FileExtensionFilter("csv"));
                if(exportFile!=null)
                    fileChooser.setSelectedFile(new File(exportFile));
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    exportFile = fileChooser.getSelectedFile().getPath();
                    visualizer.exportCSV(exportFile);
                }
            }
            if(e.getActionCommand().equals("weka export")){
                visualizer.weka();
            }
        }
    }

}
