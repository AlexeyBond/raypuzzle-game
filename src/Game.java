import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.glu.GLU;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Game
{
	protected static boolean	isRunning = true;

	protected static Grid		myGrid;

	// App. entry point //
	public static void main(String[] args)
		throws org.lwjgl.LWJGLException {
		// Create display //
		Display.setTitle("RAY");
		Display.setResizable(true);
		Display.setDisplayMode(new DisplayMode(500, 500));
		Display.setVSyncEnabled(true);
		Display.setFullscreen(false);
		//
		Display.create();

		// Setup OpenGL state //
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_BLEND);
		glClearColor(0f, 0f, 0f, 0f);

		onResize( );

		//  //
		init( );

		// Main loop //
		while( isRunning && !Display.isCloseRequested( ) ) {

			//  //
			if (Display.wasResized())
				onResize( );

			//  //
			draw( );

			//  //
			Display.update();
			Display.sync( 60 );

			//  //
			update( );
		};
	};

	private static void onResize( ) {
		int h = Display.getHeight( );
		int w = Display.getWidth( );

		glMatrixMode( GL_PROJECTION );
		glLoadIdentity( );
		glOrtho( 0.0, w, h, 0.0, -1.0, 1.0 );
		glMatrixMode( GL_MODELVIEW );
		glViewport( 0, 0, w, h );
	};

	private static void init( ) {
		//  //
		Keyboard.enableRepeatEvents( true );

		// Create grid //
		myGrid = new Grid( 20, 20 );

		// And test objects //
		Grid.CellIterator cit = myGrid.makeIterator( );
		cit.goTo( 1, 17 );
		//cit.insert( CellClass.EMITTER );
	};

	private static void draw( ) {
		glClear( GL_COLOR_BUFFER_BIT );
		myGrid.draw( 0,0, Display.getWidth(), Display.getHeight() );
	};

	private static void update( ) {
		
		//  //
		myGrid.update( );

		//  //
		while( Keyboard.next() ) {
			if( Keyboard.getEventKeyState()||Keyboard.isRepeatEvent() ) {
				// Key down or repeat event
				switch( Keyboard.getEventKey() ) {
					case Keyboard.KEY_LEFT:
						myGrid.getSelectionIterator().move(Direction.DIR_LEFT,true);
						break;
					case Keyboard.KEY_RIGHT:
						myGrid.getSelectionIterator().move(Direction.DIR_RIGHT,true);
						break;
					case Keyboard.KEY_UP:
						myGrid.getSelectionIterator().move(Direction.DIR_UP,true);
						break;
					case Keyboard.KEY_DOWN:
						myGrid.getSelectionIterator().move(Direction.DIR_DOWN,true);
						break;
					case Keyboard.KEY_R:
						if( Keyboard.isKeyDown( Keyboard.KEY_LCONTROL ) ||
							Keyboard.isKeyDown( Keyboard.KEY_RCONTROL ) ) {
							myGrid.getSelectionIterator().rotateCell( -1 );
						} else {
							myGrid.getSelectionIterator().rotateCell( 1 );
						};
						break;
					case Keyboard.KEY_SPACE:
						myGrid.getSelectionIterator().insert( CellClass.EMPTY );
						break;
					case Keyboard.KEY_E:
						if( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) ||
							Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
							myGrid.getSelectionIterator().insert( CellClass.EMITTER_P );
						} else {
							myGrid.getSelectionIterator().insert( CellClass.EMITTER_M );
						};
						break;
					case Keyboard.KEY_P:
						myGrid.getSelectionIterator().insert( CellClass.PRISM );
						break;
					case Keyboard.KEY_W:
						myGrid.getSelectionIterator().insert( CellClass.WALL );
						break;
					case Keyboard.KEY_X:
						myGrid.getSelectionIterator().insert( CellClass.RANDOMIZER );
						break;
					default:
						break;
				};
			};
		};
	};
};