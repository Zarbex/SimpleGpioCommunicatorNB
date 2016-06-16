package reader;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JRadioButton;
import javax.swing.JList;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class ReaderGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4761140581619492365L;
	private JPanel contentPane;
	private static JRadioButton rdDataPin[];
	private static JRadioButton rdSyncPin;

	final GpioController gpio;

	GpioPinDigitalInput syncPin;
	
        GpioPinDigitalInput dataPin[];
	final Data data = new Data();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ReaderGUI frame = new ReaderGUI(null, null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ReaderGUI(Pin syncPinPin, Pin dataPinPin[]) {
                this.dataPin = new GpioPinDigitalInput[dataPinPin.length];
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel pnPinStates = new JPanel();
		contentPane.add(pnPinStates, BorderLayout.NORTH);
		pnPinStates.setLayout(new GridLayout(1, 0, 0, 0));

		rdSyncPin = new JRadioButton("Sync-Pin's State");
		pnPinStates.add(rdSyncPin);

		rdDataPin = new JRadioButton[dataPin.length];
		for(int i = 0; i<rdDataPin.length; i++){
			rdDataPin[i] = new JRadioButton("Data-Pin #" + i + "'state");
			pnPinStates.add(rdDataPin[i]);
		}

		final JPanel pnSouth = new JPanel();
		pnSouth.setLayout(new BorderLayout(0, 0));
		
		
		final JTextField txtMessage = new JTextField();
		pnSouth.add(txtMessage, BorderLayout.CENTER);
		txtMessage.setColumns(10);

		final JButton btnReset = new JButton("Reset");
		btnReset.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				System.out.println("Resetting");
				data.reset();
				txtMessage.setText("");
			}
		});
		pnSouth.add(btnReset, BorderLayout.EAST);
		
		contentPane.add(pnSouth, BorderLayout.SOUTH);
		
		JScrollPane jsv = new JScrollPane();

		JList<String> listReceivedData = new JList<>();
		jsv.setViewportView(listReceivedData);
		contentPane.add(jsv, BorderLayout.CENTER);

		listReceivedData.setModel(data);
		gpio = GpioFactory.getInstance();

                this.syncPin = gpio.provisionDigitalInputPin(syncPinPin);
                
                for(int i = 0; i < this.dataPin.length; i++){
                    this.dataPin[i] = gpio.provisionDigitalInputPin(dataPinPin[i]);
                }
                
		syncPin.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event) {
				rdSyncPin.setSelected((event.getState() + "").equals("HIGH"));
				System.out.print("Sync Pin triggered " + event.getState());
				if ((event.getState() + "").equals("HIGH")) {
					for(int i = 0; i<dataPin.length; i++){
						System.out.print("; DataPin #" + i +" state: " + dataPin[i].getState());
						data.add(dataPin[i].getState() + "");
					}
					txtMessage.setText(data.toString());
				}
				System.out.println();
			}
		});

		for(int i = 0; i<dataPin.length; i++){
			dataPin[i].addListener(new GpioPinListenerDigital() {
				@Override
				public void handleGpioPinDigitalStateChangeEvent(
						GpioPinDigitalStateChangeEvent event) {
					for(int i = 0; i<dataPin.length; i++){
						if((event.getPin().getName()).equals( dataPin[i].getName())){
							//System.out.println("DataPin #" + i + " went " + event.getState());
							rdDataPin[i].setSelected((event.getState() + "").equals("HIGH"));
						}
					}
				}
			});
		}
	}

}
