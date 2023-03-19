import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.io.*;
import java.net.*;


public class MusicBox extends JFrame implements Runnable, ActionListener, AdjustmentListener{

    int rows = 37, cols = 50;
    JToggleButton[][] noteButtons;
    JPanel notePanel;
    JScrollPane scrollPane;
    String[] instrumentNames, noteNames;
    Clip[] clips;

    JMenuBar menuBar;
    JPanel colPanel, playPanel;
    JLabel colLabel;
    JMenu instrMenu, fileMenu;
    ArrayList<JMenuItem> instrItems;
    JMenuItem saveItem, loadItem;
    JButton playButton, resetButton, addColButton, remColButton, addMoreColsButton, remMoreColsButton;
    boolean isPlaying = false;

    JPanel speedPanel;
    JLabel speedLabel;
    JScrollBar speedScroll;
    int speed;

    String curDir = System.getProperty("user.dir") + "\\saved_tunes";
    JFileChooser fileChooser = new JFileChooser(curDir);

    int colCount = 0;

    Thread timing;

    public MusicBox(){       
        setNoteGrid();
        loadClips(0);
        createMenu();
        this.setSize(1000,600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        timing = new Thread(this);
        timing.start();
    }

    public void setNoteGrid() {
        notePanel = new JPanel(new GridLayout(rows,cols));
        noteButtons = new JToggleButton[rows][cols];

        String[] octave = new String[]{"C", "B", "A#", "A", "G#", "G", "F#", "F", "E", "D#", "D", "C#"};

        noteNames = new String[rows];
        instrumentNames = new String[]{"Bell", "Glockenspiel", "Marimba", "Oboe", "Oh_Ah", "Piano"};

        int o = 4;
        for (int r = 0; r < noteButtons.length; r++) {
            for (int c = 0; c < noteButtons[0].length; c++) {
                String name = octave[r%12] + o;
                noteButtons[r][c] = new JToggleButton(name);
                noteButtons[r][c].addActionListener(this);
                noteButtons[r][c].setPreferredSize(new Dimension(30,30));
                noteButtons[r][c].setMargin(new Insets(0,0,0,0));
                notePanel.add(noteButtons[r][c]);

                noteNames[r] = (name.contains("#")) ? name.substring(0, 1) + "Sharp" + o : name;
            }

            if(r%12 == 0)
                o--;
        }

        scrollPane = new JScrollPane(notePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void loadClips(int instr){
        clips = new Clip[noteNames.length];
        String chosenInstr = instrumentNames[instr];
        try {
            for(int x=0;x<noteNames.length;x++){
                URL url = this.getClass().getClassLoader().getResource("tones\\"+chosenInstr+"\\"+chosenInstr+" - "+noteNames[x]+".wav");
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                clips[x] = AudioSystem.getClip();
                clips[x].open(audioIn);
            }
        } catch (UnsupportedAudioFileException|IOException|LineUnavailableException e) {}
    }

    public void createMenu(){
        menuBar = new JMenuBar();

        fileMenu = new JMenu("File");
        saveItem = new JMenuItem("Save");
        loadItem = new JMenuItem("Load");
        saveItem.addActionListener(this);
        loadItem.addActionListener(this);
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);

        instrMenu = new JMenu("Instruments");
        instrItems = new ArrayList<JMenuItem>();
        for (int i = 0; i < instrumentNames.length; i++) {
            instrItems.add(new JMenuItem(instrumentNames[i]));
            instrItems.get(i).addActionListener(this);
            instrMenu.add(instrItems.get(i));
        }
        menuBar.add(instrMenu);

        playPanel = new JPanel();

        playButton = new JButton("Play");
        playButton.addActionListener(this);
        playPanel.add(playButton);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        playPanel.add(resetButton);

        menuBar.add(playPanel);

        colPanel = new JPanel();

        colLabel = new JLabel("Columns: " + cols);
        colPanel.add(colLabel);

        remMoreColsButton = new JButton("-?");
        remMoreColsButton.addActionListener(this);
        colPanel.add(remMoreColsButton);

        remColButton = new JButton("-1");
        remColButton.addActionListener(this);
        colPanel.add(remColButton);

        addColButton = new JButton("+1");
        addColButton.addActionListener(this);
        colPanel.add(addColButton);

        addMoreColsButton = new JButton("+?");
        addMoreColsButton.addActionListener(this);
        colPanel.add(addMoreColsButton);

        menuBar.add(colPanel);

        this.add(menuBar, BorderLayout.NORTH);

        speedPanel = new JPanel();
        speedPanel.setLayout(new BorderLayout());
        speedScroll = new JScrollBar(JScrollBar.HORIZONTAL, 200, 0, 50, 350);
        speedScroll.addAdjustmentListener(this);
        speed = speedScroll.getValue();
        speedLabel = new JLabel("Tempo: " +speed);
        speedLabel.setPreferredSize(new Dimension(150, 15));

        speedPanel.add(speedLabel, BorderLayout.WEST);
        speedPanel.add(speedScroll, BorderLayout.CENTER);
        this.add(speedPanel, BorderLayout.SOUTH);               
    }
    
    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if(e.getSource() == speedScroll){
            speed = speedScroll.getValue();
            speedLabel.setText("Tempo: " +speed);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(instrItems.contains(e.getSource())){
            int ind = 0;
            for (int i = 0; i < instrItems.size(); i++) {
                if(instrItems.get(i) == e.getSource()){
                    ind = i;
                    break;
                }
            }
            loadClips(ind);
            resetBox();
        }
        if(playButton == e.getSource()){
            isPlaying = !isPlaying;
            if(isPlaying){
                playButton.setText("Stop");
            }
            else
                playButton.setText("Play");
        }
        if(resetButton == e.getSource()){
            resetBox();
            cols = 50;
            colLabel.setText("Columns: " +cols);
            char[][] reset = new char[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    reset[r][c] = '-';
                }
            }
            refillNoteGrid(reset);
        }
        if(saveItem == e.getSource()){
            saveFile();
        }
        if(loadItem == e.getSource()){
            resetBox();
            loadFile();
        }
        if(addColButton == e.getSource()){
            cols++;
            changeColNum(rows, cols-1);
            colLabel.setText("Columns: " +cols);
        }
        if(remColButton == e.getSource()){
            cols--;
            changeColNum(rows, cols);
            colLabel.setText("Columns: " +cols);
        }
        if(addMoreColsButton == e.getSource()){
            String dialog = (String)JOptionPane.showInputDialog(this, "Enter the number of columns you wish to add. ", "Add Columns", JOptionPane.PLAIN_MESSAGE);
            int num;
            try{
                num = Integer.parseInt(dialog);
                if(num < 1)
                    throw new NumberFormatException();

                cols += num;
                changeColNum(rows, cols-num);
                colLabel.setText("Columns: " +cols);
            }catch(NumberFormatException nfe){
                if(dialog != null){
                    JOptionPane.showMessageDialog(this, "Please enter a valid number of columns.");
                }            
            }
        }
        if(remMoreColsButton == e.getSource()){
            String dialog = (String)JOptionPane.showInputDialog(this, "Enter the number of columns you wish to remove. ", "Remove Columns", JOptionPane.PLAIN_MESSAGE);
            int num;
            try{
                
                num = Integer.parseInt(dialog);

                if(num < 1 || num > cols-1)
                    throw new NumberFormatException();

                cols -= num;
                changeColNum(rows, cols);
                colLabel.setText("Columns: " +cols);
            }catch(NumberFormatException nfe){
                if(dialog != null){
                    JOptionPane.showMessageDialog(this, "Please enter a valid number of columns.");
                }
            }
        }
    }

    public void changeColNum(int rNum, int cNum){
        char[][] notes = new char[rNum][cNum];
        for (int r = 0; r < rNum; r++) {
            for (int c = 0; c < cNum; c++) {
                notes[r][c] = (noteButtons[r][c].isSelected()) ? 'x' : '-';
            }
        }
        refillNoteGrid(notes);
        revalidate();
    }

    public void saveFile(){
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", ".txt");
        fileChooser.setFileFilter(filter);

        if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile(); 
            String path = selectedFile.getAbsolutePath();
            if(path.contains("."))
                path = path.substring(0, path.indexOf("."));  

            String curSong = "";
            String[] notesNames = {" ","c ","b ","a-","a ","g-","g ","f-","f ","e ","d-","d ","c-","c ","b ","a-","a ","g-","g ","f-","f ","e ","d-","d ","c-","c ","b ","a-","a ","g-","g ","f-","f ","e ","d-","d ","c-","c "};
            for (int r = 0; r < noteButtons.length; r++) {
                // add tempo and col num to first line and note name to other lines
                curSong += (r == 0) ? String.valueOf(speed) + " " +String.valueOf(cols) +"\n" : ""; 
                curSong += notesNames[r+1];
                for (int c = 0; c < noteButtons[0].length; c++) {
                    curSong += (noteButtons[r][c].isSelected()) ? "x":"-";
                }
                curSong += "\n";
            }

            try{                
                BufferedWriter outputStream = new BufferedWriter(new FileWriter(path + ".txt")) ;
                outputStream.write(curSong);
                outputStream.close();
            }catch(IOException e){}
        }
    }

    public void loadFile(){
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            try{
                File file = fileChooser.getSelectedFile();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String text = br.readLine();
                int s = Integer.parseInt(text.split(" ")[0]);
                setSpeed(s);

                int cNum = Integer.parseInt(text.split(" ")[1]);
                char[][] songNotes = new char[37][cNum];

                int r = 0;
                while((text = br.readLine()) != null){
                    for(int c = 0; c < cNum; c++){
                        songNotes[r][c] = text.charAt(c+2);
                    }
                    r++;
                }

                rows = songNotes.length;
                cols = songNotes[0].length;
                refillNoteGrid(songNotes);
                colLabel.setText("Columns: " +cols);
            }catch(IOException e){}
        }
    }

    public void refillNoteGrid(char[][] notes){
        scrollPane.remove(notePanel);
        this.remove(scrollPane);
        setNoteGrid();

        for (int r = 0; r < notes.length; r++) {
            for (int c = 0; c < notes[0].length; c++) {
                if (notes[r][c] == 'x')
                    noteButtons[r][c].setSelected(true);
            }
        }

        revalidate();
    }

    public void setSpeed(int value){
        speedScroll.setValue(value);
        speed = value;
        speedLabel.setText("Tempo: " +value);
    }

    public void resetBox(){
        colCount = 0;
        isPlaying = false;
        playButton.setText("Play");
        setSpeed(200);
    }

    @Override
    public void run() {
        while(true){
            try{
                if(isPlaying){
                    for (int i = 0; i < rows; i++) {
                        if(noteButtons[i][colCount].isSelected())
                            clips[i].start();
                    }

                    timing.sleep(speed);

                    for (int i = 0; i < rows; i++) {
                        if(noteButtons[i][colCount].isSelected()){
                            clips[i].stop();
                            clips[i].setFramePosition(0);
                        }
                    }

                    colCount++;
                    if(colCount == cols)
                        colCount = 0;
                }
                else{
                    timing.sleep(speed);
                }
            }catch(InterruptedException ie){System.out.println("caught interrupted exception");}
        }

    }

    public static void main(String[] args) {
        MusicBox m = new MusicBox();
    }


}