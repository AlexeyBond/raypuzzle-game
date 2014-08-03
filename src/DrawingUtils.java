import static org.lwjgl.opengl.GL11.*;

public class DrawingUtils
{
	static final double SQRT_2 = 1.4142135623730951;
	static final double ONE_DIV_SQRT_2 = 1.0/SQRT_2;

	//
	public static void		drawVertices	( int x, int y, int[][] vertices, Direction dir, int scale ) {
		int dirx = dir.getDx( ) * scale;
		int diry = dir.getDy( ) * scale;
		if( (dirx!=0.0)&&(diry!=0.0) ) {
			double _dirx = dirx * ONE_DIV_SQRT_2;
			double _diry = diry * ONE_DIV_SQRT_2;
			for( int[] v : vertices ) {
				int vx = v[0], vy = v[1];
				if( v.length >= 5 ) {
					glColor3ub( (byte)v[2], (byte)v[3], (byte)v[4] );
				};
				glVertex2d( x + _dirx * vx + _diry * vy, y + _diry * vx - _dirx * vy );
			};
		} else {
			for( int[] v : vertices ) {
				int vx = v[0], vy = v[1];
				if( v.length >= 5 ) {
					glColor3ub( (byte)v[2], (byte)v[3], (byte)v[4] );
				};
				glVertex2i( x + dirx * vx + diry * vy, y + diry * vx - dirx * vy );
			};
		};
	};
};
