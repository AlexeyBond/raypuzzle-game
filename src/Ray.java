import static org.lwjgl.opengl.GL11.*;


public class Ray
{
	public static int ray_max = 15;
	protected int	ray_r;
	protected int	ray_g;
	protected int	ray_b;

	private void _validate( ) {
		if( ray_r < 0 )
			ray_r = 0;
		if( ray_g < 0 )
			ray_g = 0;
		if( ray_b < 0 )
			ray_b = 0;
		if( ray_r > ray_max )
			ray_r = ray_max;
		if( ray_g > ray_max )
			ray_g = ray_max;
		if( ray_b > ray_max )
			ray_b = ray_max;
	};

	public Ray( ) {
		reset( );
	};

	public int		getR( ) {
		return ray_r;
	};

	public int		getG( ) {
		return ray_g;
	};

	public int		getB( ) {
		return ray_b;
	};

	public void		set( int r, int g, int b ) {
		ray_r = r;
		ray_g = g;
		ray_b = b;
		_validate( );
	};

	public void		set( Ray ray ) {
		ray_r = ray.getR( );
		ray_g = ray.getG( );
		ray_b = ray.getB( );
	};

	public void		reset( ) {
		ray_r = ray_g = ray_b = 0;
	};

	public boolean	isEmpty( ) {
		return (ray_r+ray_g+ray_b) == 0;
	};

	public void		fade( ) {
		if( ray_r != 0 ) --ray_r;
		if( ray_g != 0 ) --ray_g;
		if( ray_b != 0 ) --ray_b;
	};

	public void		setGLColor( ) {
		glColor3ub(
			(byte)(30 + 15 * ray_r),
			(byte)(30 + 15 * ray_g),
			(byte)(30 + 15 * ray_b) );
	};
};
