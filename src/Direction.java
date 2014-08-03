public enum Direction {
	DIR_UP( 0 ),
	DIR_UP_RIGHT( 1 ),
	DIR_RIGHT( 2 ),
	DIR_RIGHT_DOWN( 3 ),
	DIR_DOWN( 4 ),
	DIR_LEFT_DOWN( 5 ),
	DIR_LEFT( 6 ),
	DIR_UP_LEFT( 7 );

	public static Direction DIR_RIGHT_UP = DIR_UP_RIGHT;
	public static Direction DIR_DOWN_LEFT = DIR_LEFT_DOWN;
	public static Direction DIR_DOWN_RIGHT = DIR_RIGHT_DOWN;
	public static Direction DIR_LEFT_UP = DIR_UP_LEFT;

	public static int num_directions = 8;

	private static int _direction_id_mask = 7;

	private static Direction[] _dirs =
		{DIR_UP, DIR_UP_RIGHT, DIR_RIGHT, DIR_RIGHT_DOWN,
		DIR_DOWN, DIR_LEFT_DOWN, DIR_LEFT, DIR_UP_LEFT };

	private static int[] _dir_dx =
		{0,1,1,1,0,-1,-1,-1};
	private static int[] _dir_dy =
		{-1,-1,0,1,1,1,0,-1};

	private Direction _dir_get( int id ) {
		if( id < 0 )
			return _dir_get( id + num_directions );
		return _dirs[id & _direction_id_mask];
	};

	Direction( int id ) {
		dir_id = id;
	};

	private int dir_id;

	public int			getId( ) {
		return dir_id;
	};

	public Direction	next( ) {
		return _dir_get( dir_id + 1 );
	};

	public Direction	rotate( int n ) {
		return _dir_get( dir_id + n );
	};

	public Direction	reverse( ) {
		return _dir_get( dir_id + 4 );
	};

	public Direction	rotate90CW( ) {
		return _dir_get( dir_id + 2 );
	};

	public Direction	rotate90CCW( ) {
		return _dir_get( dir_id - 2 );
	};

	public int			getDx( ) {
		return _dir_dx[dir_id];
	};

	public int			getDy( ) {
		return _dir_dy[dir_id];
	};
};
