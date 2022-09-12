import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class musicSynth {

        JPanel mainPanel;
        //We store the checkboxes in the arrayList
        ArrayList<JCheckBox> checkboxList;
        Sequencer sequencer;
        Sequence sequence;
        Track track;
        JFrame theFrame;
        //These are the names of the instruments that are used as labels for the gui panel
        String[] instrumentNames = {" Bass Drum", "Closed Hi-Hat","Open Hi-Hat",
                "Acoustic Snare", "Crash Cymbal", "Hand Clap","High Tom", " Hi Bongo", "Maracas","Whistle", " Low Conga", "Cowbell", "Vibraslap", "Low- mid Tom", " High Agogo",
                "Open Hi Conga"
        };
    //These represent the actual drum keys. The drum channel is like a piano except each key on the piano is different.
        int[]instruments={35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

        public static void main(String[]args){
            new musicSynth().buildGUI();
        }

        public void buildGUI(){
            theFrame=new JFrame("Cyber BeatBox");
            theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BorderLayout layout=new BorderLayout();
            JPanel background=new JPanel(layout);
            //An empty border gives us the margin betweeen the edge of the panel and where the components are placed
            background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            checkboxList=new ArrayList<JCheckBox>();
            Box buttonBox=new Box(BoxLayout.Y_AXIS);

            JButton start=new JButton("Start");
            start.addActionListener(new MyStartListener());
            buttonBox.add(start);

            JButton stop=new JButton("Stop");
            stop.addActionListener(new MyStopListener());
            buttonBox.add(stop);

            JButton upTempo=new JButton("Tempo Up");
            upTempo.addActionListener(new MyUpTempoListener());
            buttonBox.add(upTempo);

            JButton downTempo=new JButton("Tempo Down");
            downTempo.addActionListener(new MyDownTempoListener());
            buttonBox.add(downTempo);

            Box nameBox = new Box(BoxLayout.Y_AXIS);
            for (int i = 0; i < 16; i++) {
                nameBox.add(new Label(instrumentNames[i]));
            }

            background.add(BorderLayout.EAST, buttonBox);
            background.add(BorderLayout.WEST, nameBox);
            theFrame.getContentPane().add(background);

            GridLayout grid = new GridLayout(16,16);
            grid.setVgap(1);
            grid.setHgap(2);
            mainPanel = new JPanel(grid);
            background.add(BorderLayout.CENTER, mainPanel);

            //Makes the checkboxes, set them to false and add them to the ArrayList and to the GUI panel
            for (int i = 0; i < 256; i++) {
                JCheckBox c = new JCheckBox();
                c.setSelected(false);
                checkboxList.add(c);
                mainPanel.add(c);
            } // end loop
            setUpMidi();
            theFrame.setBounds(50,50,300,300);
            theFrame.pack();
            theFrame.setVisible(true);
        } // close method
        public void setUpMidi() {
            try {
                sequencer = MidiSystem.getSequencer();
                sequencer.open();
                sequence = new Sequence(Sequence.PPQ,4);
                track = sequence.createTrack();
                sequencer.setTempoInBPM(120);

            } catch(Exception e) {e.printStackTrace();}
        } // close method

        public void buildTrackAndStart() {
            //We make a 16 element array to hold the value for one instrument, across all 16 beats. if the instrument
            //is supposed to play on the beat, the key will be the value of the element
            int[] trackList = null;

            //Get rid of the old tracks, makes a fresh one
            sequence.deleteTrack(track);
            track = sequence.createTrack();

            //do this for all 16 rows
            for (int i = 0; i < 16; i++) {
                trackList = new int[16];
                //The key that represents which instrument it is
                int key = instruments[i];
                for (int j = 0; j < 16; j++ ) {

                    JCheckBox jc = checkboxList.get(j + 16*i);
                    //if the checkbox is selected, put a key value in the slot in the array
                    if ( jc.isSelected()) {
                        trackList[j] = key;
                    } else {
                        trackList[j] = 0;
                    }
                } // close inner loop

                //for this instrument and for all 16 beats make events and add them to the track
                makeTracks(trackList);
                track.add(makeEvent(176,1,127,0,16));
            } // close outer
            track.add(makeEvent(192,9,1,0,15));
            try {
                sequencer.setSequence(sequence);
                sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
                sequencer.start();
                sequencer.setTempoInBPM(120);
            } catch(Exception e) {e.printStackTrace();}
        } // close buildTrackAndStart method


        public class MyStartListener implements ActionListener {
            public void actionPerformed(ActionEvent a) {
                buildTrackAndStart();
            }
        } // close inner class
        public class MyStopListener implements ActionListener {
            public void actionPerformed(ActionEvent a) {
                sequencer.stop();
            }
        } // close inner class
        public class MyUpTempoListener implements ActionListener {
            public void actionPerformed(ActionEvent a) {
                float tempoFactor = sequencer.getTempoFactor();
                sequencer.setTempoFactor((float)(tempoFactor * 1.03));
            }
        } // close inner class
        public class MyDownTempoListener implements ActionListener {
            public void actionPerformed(ActionEvent a) {
                float tempoFactor = sequencer.getTempoFactor();
                sequencer.setTempoFactor((float)(tempoFactor * .97));
            }
        } // close inner class

    //This method maes an event for one of the instruments at a time for all 16 beats.
    //if the event is zero at any point then the element is not suppose to play anything
        public void makeTracks(int[] list) {

            for (int i = 0; i < 16; i++) {
                int key = list[i];
                if (key != 0) {
                    track.add(makeEvent(144,9,key, 100, i));
                    track.add(makeEvent(128,9,key, 100, i+1));
                }
            }
        }

        public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
            MidiEvent event = null;
            try {
                ShortMessage a = new ShortMessage();
                a.setMessage(comd, chan, one, two);
                event = new MidiEvent(a, tick);
            } catch(Exception e) {e.printStackTrace(); }
            return event;
        }
    } // close class
