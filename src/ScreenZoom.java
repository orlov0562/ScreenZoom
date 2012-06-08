import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;


public class ScreenZoom {
	public static void main(String[] args) {
		new Window("Capture test");
	}
}

class Config {
	public static int captureSizeX = 240;
	public static int captureSizeY = 320;
	public static int captureBorder = 2;	
	public static int zoom = 200;
	public static int windowWidth = captureSizeX*zoom/100+100;
	public static int windowHeight = captureSizeY*zoom/100+100;
	public static int captureDelay = (int) 1000 / 30;
	public static Window mainWindow;
	
}

class Window extends JFrame
{
	private static final long serialVersionUID = 1L;

	Window(String title)
	{
		super(title);
		setTitle("Zoom "+Config.zoom+"%");
		Config.mainWindow = this;		
		setSize(Config.windowWidth,Config.windowHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImagePanel panel = new ImagePanel(Config.windowWidth,Config.windowHeight);
		add(panel);
		new Thread((new SwingCapture(panel))).start();
		setAlwaysOnTop(true);
		setVisible(true);
	}
	
	
}

class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private boolean resize = true;
	private drawBorderOnDesktop border=null;

    ImagePanel(int x, int y) {
    	setBounds(0,0,x,y);
    	setDoubleBuffered(true);
    	image = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
    	
    	addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Config.zoom+=(e.getWheelRotation()*-1)*5;
				if (Config.zoom<10) Config.zoom=10;
				Config.mainWindow.setTitle("Zoom "+Config.zoom+"%");
				Graphics g = getGraphics();
				g.setColor( getBackground() );
				g.fillRect (0, 0, getWidth(), getHeight());
				g.setColor( Color.GRAY);
				g.drawRect (0, 0, image.getWidth()*Config.zoom/100, image.getHeight()*Config.zoom/100);
				g.drawLine (0, 0, image.getWidth()*Config.zoom/100, image.getHeight()*Config.zoom/100);
				g.drawLine (image.getWidth()*Config.zoom/100, 0, 0, image.getHeight()*Config.zoom/100);

				Config.windowWidth = Config.captureSizeX*Config.zoom/100+100;
				Config.windowHeight = Config.captureSizeY*Config.zoom/100+100;
				Config.mainWindow.setSize(Config.windowWidth, Config.windowHeight);
				setSize(Config.windowWidth, Config.windowHeight);
				
			}
		});
    	
    	addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseClicked(MouseEvent e) {
    			super.mouseClicked(e);
    			if (e.getButton()==MouseEvent.BUTTON2) {
    				resize = !resize;
    				if (resize) Config.mainWindow.setTitle("Zoom "+Config.zoom+"%");
    				else Config.mainWindow.setTitle("Zoom 100%");
    			}
    			else if(e.getButton()==MouseEvent.BUTTON1)
    			{
    				Config.captureSizeX += Config.captureSizeX/10;
    				Config.captureSizeY += Config.captureSizeY/10;
    				drawBorder(Config.mainWindow.getLocation().x + Config.mainWindow.getWidth()+Config.captureSizeX/2+20,
 						   Config.mainWindow.getLocation().y+Config.captureSizeY/2);
    			}
    			else if(e.getButton()==MouseEvent.BUTTON3)
    			{
    				Config.captureSizeX -= Config.captureSizeX/10;
    				Config.captureSizeY -= Config.captureSizeY/10;
    				if (Config.captureSizeX<10) Config.captureSizeX = 10;
    				if (Config.captureSizeY<10) Config.captureSizeY = 10;
    				drawBorder(Config.mainWindow.getLocation().x + Config.mainWindow.getWidth()+Config.captureSizeX/2+20,
    						   Config.mainWindow.getLocation().y+Config.captureSizeY/2);
    			}
    			Graphics g = getGraphics();
				g.setColor( getBackground() );
				g.fillRect (0, 0, getWidth(), getHeight());
    			
				Config.windowWidth = Config.captureSizeX*Config.zoom/100+100;
				Config.windowHeight = Config.captureSizeY*Config.zoom/100+100;
				Config.mainWindow.setSize(Config.windowWidth, Config.windowHeight);
				setSize(Config.windowWidth, Config.windowHeight);
				repaint();
    			
    		}
		});
    	
    	border = new drawBorderOnDesktop(0,0,Config.captureSizeX,Config.captureSizeY);
    }

    @Override
    public void paint(Graphics g) {
    	
		g.setColor( getBackground() );
		g.fillRect (0, 0, getWidth(), getHeight());
		
    	g.drawImage( resize ? resizeImage(image, Config.zoom) : image , 5, 5, null);
    	
    	int cx = 5+(Config.captureSizeX/2)*Config.zoom/100;
    	int cy = 5+(Config.captureSizeY/2)*Config.zoom/100;

    	g.setColor( Color.WHITE );
    	g.fillRect (cx-1, cy-2, 5,5);
    	
    	
    	g.setColor( Color.GRAY );
    	g.drawLine (cx-5+1,cy-5,cx+5+1,cy+5);
    	g.drawLine (cx-5+1,cy+5,cx+5+1,cy-5);

    	g.setColor( Color.BLACK );
    	g.fillRect (cx, cy-1, 3,3);
    	
    	
    }
    
    private BufferedImage resizeImage(BufferedImage image, int zoom)
    {
    	int x = image.getWidth()*zoom/100; 
    	int y = image.getHeight()*zoom/100;
    	
    	// готовим изображение, на которое будет выводить изображение с измененными размерами
    	BufferedImage resizedImage = new BufferedImage(x,y, BufferedImage.TYPE_INT_ARGB);
    	
    	Graphics2D gr = resizedImage.createGraphics();
    	gr.drawImage(image, 0, 0, x, y, null);

    	gr.dispose();
    	gr.setComposite(AlphaComposite.Src);
    	gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    	gr.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    	gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		return resizedImage;
    }
    
    public BufferedImage getImage() {
    	return image;
    }
    
    public void setImage(BufferedImage image) {
    	this.image = image;
    	repaint();
    }
    
	public void drawBorder(int rx, int ry) {
		if (border==null) return;
		border.setPosition( rx-Config.captureSizeX/2, ry-Config.captureSizeY/2,
							rx+Config.captureSizeX/2, ry+Config.captureSizeY/2);
	}    
    
}

class SwingCapture implements Runnable {
	ImagePanel panel;
	
	SwingCapture(ImagePanel panel) {
		this.panel = panel;	
	}
	
	@Override
	public void run() {
		do {
			try {
				SwingUtilities.invokeAndWait(new ImageProcessor(panel));
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sleep(Config.captureDelay);
		} while(true);
	}
	
	private void sleep(int msec){
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
}

class ImageProcessor implements Runnable {
	private ImagePanel panel;
	
	ImageProcessor(ImagePanel panel) {
		this.panel = panel;
	}
	
	@Override
	public void run() {
		try {
			int rx = MouseInfo.getPointerInfo().getLocation().x;
			int ry = MouseInfo.getPointerInfo().getLocation().y;
			
			if (	( (rx+Config.captureSizeX/2)>Config.mainWindow.getLocation().x ) 
					&& ( (rx-Config.captureSizeX/2)<(Config.mainWindow.getLocation().x+Config.mainWindow.getWidth()))
					&& ( (ry+Config.captureSizeY/2)>Config.mainWindow.getLocation().y )
					&& ( (ry-Config.captureSizeY/2)<(Config.mainWindow.getLocation().y+Config.mainWindow.getHeight()))
			) return;
			
			panel.drawBorder(rx,ry);
			Rectangle captureSize = new Rectangle(rx-Config.captureSizeX/2, ry-Config.captureSizeY/2, Config.captureSizeX+Config.captureBorder, Config.captureSizeY+Config.captureBorder);			
			BufferedImage image = (new Robot()).createScreenCapture(captureSize);
			panel.setImage(image);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
}

class drawBorderOnDesktop {
	public JWindow topLine;
	public JWindow bottomLine;
	public JWindow leftLine;
	public JWindow rightLine;
	drawBorderOnDesktop(int x, int y, int w, int h) {
		topLine = new JWindow();
		topLine.setBounds(x,y,w-x,1);
		topLine.getContentPane().setBackground(Color.BLACK);
		topLine.setVisible(true);
		topLine.setAlwaysOnTop(true);
		
		bottomLine = new JWindow();
		bottomLine.setBounds(x,h,w-x,1);
		bottomLine.getContentPane().setBackground(Color.BLACK);
		bottomLine.setVisible(true);
		bottomLine.setAlwaysOnTop(true);

		leftLine = new JWindow();
		leftLine.setBounds(x,y,1,h-y);
		leftLine.getContentPane().setBackground(Color.BLACK);
		leftLine.setVisible(true);
		leftLine.setAlwaysOnTop(true);

		rightLine = new JWindow();
		rightLine.setBounds(w,y,1,h-y);
		rightLine.getContentPane().setBackground(Color.BLACK);
		rightLine.setVisible(true);
		rightLine.setAlwaysOnTop(true);
		
		
	}
	
	public void setPosition(int x, int y, int w, int h) {
		
		topLine.setBounds(x,y,w-x,Config.captureBorder);
		topLine.setLocation(x, y);
		
		bottomLine.setBounds(x,h,w-x,Config.captureBorder);
		bottomLine.setLocation(x, h);

		leftLine.setBounds(x,y,Config.captureBorder,h-y);
		leftLine.setLocation(x, y);

		rightLine.setBounds(w,y,Config.captureBorder,h-y+Config.captureBorder);
		rightLine.setLocation(w, y);
		
	}
	
}