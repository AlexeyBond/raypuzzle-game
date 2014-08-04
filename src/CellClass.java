import static org.lwjgl.opengl.GL11.*;

import java.lang.Math;

public enum CellClass
{
	EMPTY {
		public void update( Grid.CellIterator iter ) {
			for( Direction dir : Direction.values() ) {
				Ray recv = iter.getStat().getReceived( dir.reverse() );
				if( recv != null ) {
					recv.fade( );
					iter.send( dir, recv );
				};
			};
		};
		public void		draw( Grid.CellIterator iter, int x, int y, int size ) {
			// Draw nothing //
		};
	},
	EMITTER_P {
		public void update( Grid.CellIterator iter ) {
			Ray _dest = iter.getDestination( iter.getStat().getDirection() );
			Ray incomming = iter.getStat().getReceived( iter.getStat().getDirection().reverse() );
			if( _dest != null ) {
				int tick = (int)iter.getGridTick( );
				int r = emit_pattern[tick%30]
					- incomming.getR();
				int g = emit_pattern[(tick+10)%30]
					- incomming.getG();
				int b = emit_pattern[(tick+20)%30]
					- incomming.getB();
				_dest.set( r, g, b );
			};
		};
		public void		draw( Grid.CellIterator iter, int x, int y, int size ) {
			glBegin( GL_TRIANGLE_FAN );
			DrawingUtils.drawVertices( x + (size>>1) + 1, y + (size>>1) + 1,
				emitter_vertices, iter.getStat().getDirection(), size>>3 );
			glEnd( );
		};
	},
	EMITTER_M {
		public void update( Grid.CellIterator iter ) {
			Ray _dest = iter.getDestination( iter.getStat().getDirection() );
			Ray incomming = iter.getStat().getReceived( iter.getStat().getDirection().reverse() );
			if( _dest != null ) {
				int tick = (int)iter.getGridTick( );
				int r = Ray.ray_max - incomming.getR();
				int g = Ray.ray_max - incomming.getG();
				int b = Ray.ray_max - incomming.getB();
				_dest.set( r, g, b );
			};
		};
		public void		draw( Grid.CellIterator iter, int x, int y, int size ) {
			glBegin( GL_TRIANGLE_FAN );
			DrawingUtils.drawVertices( x + (size>>1) + 1, y + (size>>1) + 1,
				emitter_vertices, iter.getStat().getDirection(), size>>3 );
			glEnd( );
		};
	},
	PRISM {
		public void update( Grid.CellIterator iter ) {
			Ray dest_r = iter.getDestination( iter.getStat().getDirection().rotate(-1) );
			Ray dest_g = iter.getDestination( iter.getStat().getDirection() );
			Ray dest_b = iter.getDestination( iter.getStat().getDirection().rotate(+1) );
			Ray dest_a = iter.getDestination( iter.getStat().getDirection().reverse() );

			Ray in_r = iter.getStat().getReceived( iter.getStat().getDirection().rotate(-1) );
			Ray in_g = iter.getStat().getReceived( iter.getStat().getDirection() );
			Ray in_b = iter.getStat().getReceived( iter.getStat().getDirection().rotate(+1) );
			Ray in_a = iter.getStat().getReceived( iter.getStat().getDirection().reverse() );

			in_a.fade( );

			dest_r.set( in_a.getR(), 0, 0 );
			dest_g.set( 0, in_a.getG(), 0 );
			dest_b.set( 0, 0, in_a.getB() );

			in_b.fade( );
			in_g.fade( );
			in_r.fade( );

			dest_a.set( in_b );
			dest_a.add( in_g );
			dest_a.add( in_r );
		};
		public void		draw( Grid.CellIterator iter, int x, int y, int size ) {
			glBegin( GL_TRIANGLES );
			DrawingUtils.drawVertices( x + (size>>1) + 1, y + (size>>1) + 1,
				prism_vertices, iter.getStat().getDirection(), size>>3 );
			glEnd( );
		};
	},
	MIRROR {
		public void update( Grid.CellIterator iter ) {
			Grid.CellState stat = iter.getStat( );
			Direction dir = stat.getDirection( );
			{
				Ray r = stat.getReceived( dir );
				r.fade( );
				iter.send( dir, r );
				r = stat.getReceived( dir.reverse( ) );
				r.fade( );
				iter.send( dir.reverse( ), r );
			};
			{
				Ray r = stat.getReceived( dir.rotate( -1 ) );
				r.fade( );
				iter.send( dir.rotate(+1), r );
				r = stat.getReceived( dir.rotate(+1) );
				r.fade( );
				iter.send( dir.rotate(-1), r );
			};
			{
				Ray r = stat.getReceived( dir.reverse().rotate( -1 ) );
				r.fade( );
				iter.send( dir.reverse().rotate(+1), r );
				r = stat.getReceived( dir.reverse().rotate(+1) );
				r.fade( );
				iter.send( dir.reverse().rotate(-1), r );
			};
		};
		public void		draw( Grid.CellIterator iter, int x, int y, int size ) {
			glBegin( GL_LINES );
			DrawingUtils.drawVertices( x + (size>>1) + 1, y + (size>>1) + 1,
				mirror_vertices, iter.getStat().getDirection(), size>>3 );
			glEnd( );
		};
	},
	RANDOMIZER {
		public void update( Grid.CellIterator iter ) {
			int turn = (int)(Math.random()*(Direction.num_directions*100));
			for( Direction dir : Direction.values( ) ) {
				Ray recv = iter.getStat().getReceived( dir.reverse() );
				if( recv != null ) {
					recv.fade( );
					iter.send( dir.rotate(turn), recv );
				};
			};
		};
		public void		draw( Grid.CellIterator iter, int x, int y, int size ) {
			int s = (size>>2)+(size>>3);
			glBegin( GL_LINES );
			glColor3ub( (byte)255, (byte)255, (byte)255 );
			glVertex2i(x+s,y+s);
			glVertex2i(x+size-s,y+size-s);
			glVertex2i(x+s,y+size-s);
			glVertex2i(x+size-s,y+s);
			glEnd( );
		};
	},
	WALL {
		public void		draw( Grid.CellIterator iter, int x, int y, int size ) {
			int offst = (size>>2);
			glBegin( GL_QUADS );
			glColor3ub( (byte)20, (byte)20, (byte)20 );
			glVertex2i( x+offst, y+offst );
			glVertex2i( x+offst, y+size-offst );
			glVertex2i( x+size-offst, y+size-offst );
			glVertex2i( x+size-offst, y+offst );
			glEnd( );
		};
	};

	// For EMITTER_P //
	private static final int[] emit_pattern =
		{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
		  15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

	// Vertex arrays .. //
	// for PRISM
	private static int[][] prism_vertices = {
		{3,0,0,255,0},{-1,4,255,0,0},{-1,0,255,255,255},
		{-1,-4,0,0,255},{3,0,0,255,0},{-1,0,255,255,255}
	};

	// for EMITTER
	private static int[][] emitter_vertices = {
		{2,0,255,255,255},{0,2},{-2,2},{-2,-2},{0,-2}
	};

	// for MIRROR
	private static int[][] mirror_vertices = {
		{0,-3,255,255,255}, {0,3}
	};

	public void		update( Grid.CellIterator iter ) {

	};

	public void		draw( Grid.CellIterator iter, int x, int y, int size ) {
		glBegin( GL_LINES );
		glColor3ub( (byte)255, (byte)0, (byte)0 );
		glVertex2i(x+5,y+5);
		glVertex2i(x+size-5,y+size-5);
		glVertex2i(x+5,y+size-5);
		glVertex2i(x+size-5,y+5);
		glEnd( );
	};
};
