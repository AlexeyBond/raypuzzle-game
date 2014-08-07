import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.glu.GLU;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.lang.Exception;

public class Grid
{
	public class CellState
	{
		private CellClass	type;
		private Direction	dir;
		private Ray[][]		received;
		private boolean		locked;

		public CellState( ) {
			type = CellClass.EMPTY;
			dir = Direction.DIR_UP;
			locked = false;
			received = new Ray[2][Direction.num_directions];
			for( int i = 0; i < 2; ++i )
				for( int j = 0; j < Direction.num_directions; ++j )
					received[i][j] = new Ray( );
		};

		public Ray[]	getCurReceived( ) {
			return received[(int)(grid_tick&1)];
		};

		public Ray[]	getNextReceived( ) {
			return received[(int)((grid_tick^1)&1)];
		};

		public void		receive( Direction dir, Ray ray ) {
			getNextReceived()[dir.getId()].set(ray);
		};

		public Ray		getReceiver( Direction dir ) {
			return getNextReceived()[dir.getId()];
		};

		public Ray		getReceived( Direction dir ) {
			return getCurReceived()[dir.getId()];
		};

		public CellClass	getType( ) {
			return type;
		};

		public void		setType( CellClass newType ) {
			if( type != newType ) {
				type = newType;
				if( grid_renderer != null )
					grid_renderer.onCellChange( );
			};
		};

		public void		lock( boolean b ) {
			locked = b;
		};

		public boolean	isLocked( ) {
			return locked;
		};

		public Direction	getDirection( ) {
			return dir;
		};

		public void			setDirection( Direction ndir ) {
			if( ndir != dir ) {
				dir = ndir;
				if( grid_renderer != null )
					grid_renderer.onCellChange( );
			};
		};

		public void			rotate( int n ) {
			setDirection( getDirection( ).rotate( n ) );
		};
	};

	public class CellIterator
	{
		protected int		cur_x = 0;
		protected int		cur_y = 0;
		protected CellState	cur_stat = null;

		private void _update_state( ) {
			if( (cur_x < 0) ||
				(cur_y < 0) ||
				(cur_x >= grid_width) ||
				(cur_y >= grid_height) ) {
				cur_stat = null;
			} else {
				cur_stat = grid_array[cur_x+cur_y*grid_width];
			};
		}

		public CellIterator( ) {
		};

		public void			reset( ) {
			cur_x = 0;
			cur_y = 0;
			cur_stat = grid_array[0];
		};

		public int			getX( ) {
			return cur_x;
		};

		public int			getY( ) {
			return cur_y;
		};

		public CellState	getStat( ) {
			return cur_stat;
		};

		public boolean		insert( CellClass cc ) {
			if( cur_stat == null )
				return false;
			if( cur_stat.isLocked() )
				return false;
			cur_stat.setType( cc );
			return true;
		};

		public boolean		rotateCell( int num ) {
			if( cur_stat == null )
				return false;
			if( cur_stat.isLocked() )
				return false;
			cur_stat.rotate( num );
			return true;
		};

		public boolean		next( ) {
			++cur_x;
			if( cur_x >= grid_width ) {
				cur_x = 0;
				++cur_y;
			};
			_update_state( );
			return cur_stat != null;
		};

		public void			goTo( int x, int y ) {
			cur_x = x;
			cur_y = y;
			_update_state( );
		};

		public long			getGridTick( ) {
			return getTick( );
		};

		public boolean		move( Direction dir, boolean _safe ) {
			cur_x += dir.getDx( );
			cur_y += dir.getDy( );
			_update_state( );
			if( _safe && (cur_stat == null) )
				return move( dir.reverse(), false );
			return cur_stat != null;
		};

		public CellState	getStat( Direction dir ) {
			int sx = cur_x + dir.getDx( );
			int sy = cur_y + dir.getDy( );
			if( (sx < 0) ||
				(sy < 0) ||
				(sx >= grid_width) ||
				(sy >= grid_height) ) {
				return null;
			};
			return grid_array[sx+sy*grid_width];
		};

		public void			send( Direction dir, Ray ray ) {
			CellState cs = getStat( dir );
			if( cs != null )
				cs.receive( dir.reverse(), ray );
		};

		public Ray			getDestination( Direction dir ) {
			CellState cs = getStat( dir );
			if( cs != null )
				return cs.getReceiver( dir.reverse() );
			return null;
		};
	};

	public static abstract class Renderer {
		protected Grid		my_grid;
		protected Renderer( Grid grid )
			throws Exception {
			my_grid = grid;
			if( grid.grid_renderer != null ) {
				throw new Exception( "Impossible to create more than one renderer for grid." );
			};
			grid.grid_renderer = this;
		};

		public abstract void		init( );
		public abstract void		draw( );

		protected void				onLockStatusUpdate	( ){ };
		protected void				onCellChange		( ){ };
		protected void				onRaysChange		( ){ };
	};

	public static class DefaultRenderer
		extends Renderer {
		public DefaultRenderer( Grid grid )
			throws Exception {
			super( grid );
			grid_iterator = grid.makeIterator( );

			// Set default viewport & cell params //
			vp_x = 0;
			vp_y = 0;
			vp_width = 500;
			vp_height = 500;
			cell_size = 20;
			cell_offst_x = 0;
			cell_offst_y = 0;
		};

		// Viewport parameters //
		private int				vp_x, vp_y;
		private int				vp_width, vp_height;

		// Cell drawing parameters //
		private int				cell_size;
		private int				cell_offst_x, cell_offst_y;
		private static int		cell_gap = 1;
		private static int		grid_gap = 5;

		// Iterator for grid //
		private CellIterator	grid_iterator;

		// GL lists //
		private	int			dlist_background;
		private	int			dlist_cells;
		private	int			dlist_rays;

		//  //
		private void		recompileBagkground( ) {
			glNewList( dlist_background, GL_COMPILE );
			// Draw cell background //
			for( int iy = 0; iy < my_grid.grid_height; ++iy ) {
				int _base = iy * my_grid.grid_width;
				for( int ix = 0; ix < my_grid.grid_width; ++ix ) {
					int _x = cell_offst_x + cell_size * ix;
					int _y = cell_offst_y + cell_size * iy;
					{
						CellState cs = my_grid.grid_array[ix+my_grid.grid_width*iy];
						if( cs.isLocked() ) {
							glBegin( GL_QUADS );
							glColor3ub( (byte)10, (byte)10, (byte)10 );
						} else {
							glBegin( GL_LINE_LOOP );
							glColor3ub( (byte)20, (byte)50, (byte)20 );
						};
					};
					glVertex2i( _x + cell_gap, _y + cell_gap );
					glVertex2i( _x + cell_gap, _y + cell_size - cell_gap );
					glVertex2i( _x + cell_size - cell_gap, _y + cell_size - cell_gap );
					glVertex2i( _x + cell_size - cell_gap, _y + cell_gap );
					glEnd( );
				};
			};
			glEndList( );
		};

		//  //
		private void		recompileCells( ) {
			glNewList( dlist_cells, GL_COMPILE );
			// Draw cells //
			grid_iterator.reset( );
			do {
				int _x = cell_offst_x + cell_size * grid_iterator.getX();
				int _y = cell_offst_y + cell_size * grid_iterator.getY();
				grid_iterator.getStat().getType().draw( grid_iterator, _x, _y, cell_size );
			} while( grid_iterator.next( ) );
			glEndList( );
		};

		//  //
		private void		recompileRays( ) {
			glNewList( dlist_rays, GL_COMPILE );
			// Draw rays //
			grid_iterator.reset( );
			glBegin( GL_LINES );
			do {
				int _x = cell_offst_x + cell_size * grid_iterator.getX() + ((cell_size>>1)+1);
				int _y = cell_offst_y + cell_size * grid_iterator.getY() + ((cell_size>>1)+1);
				for( Direction dir : Direction.values() ) {
					Ray recv = grid_iterator.getStat( ).getReceived( dir );
					if( recv != null ) {
						if( !recv.isEmpty() ) {
							recv.setGLColor( );
							glVertex2i( _x, _y );
							glVertex2i( _x+cell_size*dir.getDx(), _y+cell_size*dir.getDy() );
						};
					};
				};
			} while( grid_iterator.next( ) );
			glEnd( );
			glEndList( );
		};

		//  //
		private void		drawSelection( ) {
			// Draw selection //
			int _x = cell_offst_x + cell_size * my_grid.getSelectionIterator().getX();
			int _y = cell_offst_y + cell_size * my_grid.getSelectionIterator().getY();

			glColor3ub( (byte)255, (byte)50, (byte)50 );
			glBegin( GL_LINE_LOOP );
			glVertex2i( _x, _y );
			glVertex2i( _x+cell_size, _y );
			glVertex2i( _x+cell_size, _y+cell_size );
			glVertex2i( _x, _y+cell_size );
			glEnd( );
		}

		//  //
		protected void				onLockStatusUpdate	( ){
			recompileBagkground( );
		};
		protected void				onCellChange		( ){
			recompileCells( );
		};
		protected void				onRaysChange		( ){
			recompileRays( );
		};

		//  //
		public void			setViewport( int x, int y, int w, int h ) {
			// Determine where and with whith size draw grid //
			int a = (w-grid_gap*2) / my_grid.grid_width;
			int b = (h-grid_gap*2) / my_grid.grid_height;
			if( a < b ) {
				cell_size = ((a&1) != 0)?a:(a-1);
				cell_offst_x = grid_gap;
				cell_offst_y = (h-cell_size*my_grid.grid_height)/2;
			} else {
				cell_size = ((b&1) != 0)?b:(b-1);
				cell_offst_y = grid_gap;
				cell_offst_x = (w-cell_size*my_grid.grid_width)/2;
			};
			// Recompile lists //
			recompileBagkground( );
			recompileCells( );
			recompileRays( );
		};

		//  //
		public void			init( ) {
			// Generate lists //
			dlist_background = glGenLists( 1 );
			dlist_cells = glGenLists( 1 );
			dlist_rays = glGenLists( 1 );

			//  //
		};

		//  //
		public void			draw( ) {
			glCallList( dlist_background );
			glCallList( dlist_rays );
			glCallList( dlist_cells );
			drawSelection( );
		};
	};

	protected int			grid_height;
	protected int			grid_width;
	protected int			grid_size;
	protected CellState		grid_array[];

	protected long			grid_tick;

	private CellIterator	_grid_update_iterator = new CellIterator( );
	private CellIterator	_grid_draw_iterator = new CellIterator( );
	private CellIterator	_grid_selection_iterator = new CellIterator( );

	private	Renderer		grid_renderer = null;

	public Grid( int width, int height ) {
		grid_width = width;
		grid_height = height;
		grid_size = width * height;
		grid_array = new CellState[grid_size];

		for( int i = 0; i < grid_size; ++i )
			grid_array[i] = new CellState( );

		for( int i = 0; i < grid_width; ++i ) {
			CellState cs;
			cs = grid_array[i];
			cs.setType( CellClass.WALL );
			cs.lock( true );
			cs = grid_array[i+grid_width*(grid_height-1)];
			cs.setType( CellClass.WALL );
			cs.lock( true );
		};
		for( int i = 0; i < grid_height; ++i ) {
			CellState cs;
			cs = grid_array[i*grid_width];
			cs.setType( CellClass.WALL );
			cs.lock( true );
			cs = grid_array[(i+1)*grid_width - 1];
			cs.setType( CellClass.WALL );
			cs.lock( true );
		};

		_grid_selection_iterator.goTo( 1, 1 );

		grid_tick = 0;
	};

	public CellIterator		getSelectionIterator( ) {
		return _grid_selection_iterator;
	};

	public CellIterator		makeIterator( ) {
		return new CellIterator( );
	};

	public long				getTick( ) {
		return grid_tick;
	};

	public void	update( ) {
		// Iterate over all cells //
		_grid_update_iterator.reset( );
		do {
			// Call update method //
			_grid_update_iterator.getStat().getType().update( _grid_update_iterator );
			// Clear received rays for next frame //
			for( Ray r : _grid_update_iterator.getStat().getCurReceived() )
				if( r != null )
					r.reset( );
		} while( _grid_update_iterator.next() );

		// Increment tick //
		++grid_tick;

		// Say to the renderer that rays are updated //
		if( grid_renderer != null )
			grid_renderer.onRaysChange( );
	};
};
