package newhorizon.feature;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.ObjectMap;
import arc.util.Time;

public class Cube{
	public static final Cube tmpCube = new Cube();
	
	protected final Vec3 direction = new Vec3(1, 1, 0.5);
	protected final Vec3 rotationVec = new Vec3(0, 0, -1);
	
	protected final int[][] verticesIndex = {
		{1, 2, 4, 3},
		{5, 1, 3, 7},
		{6, 2, 1, 5},
		{7, 3, 4, 8},
		{8, 4, 2, 6},
		{7, 8, 6, 5}
	};
	
	public Color color = Color.white;
	public float shade = 2;
	public float size = 2.5f;
	public float sizeRand = 0.45f;
	public float sizeScale = 45f;
	
	public float cubeSlideSize = 30f;
	public float cubeSlideSizeRand = 45f;
	public float cubeSlideSizeScale = 45f;
	
	public Cube(){}
	
	public Cube(Color color, float size, float sizeRand){
		this.color = color;
		this.size = size;
		this.sizeRand = sizeRand;
	}
	
	public Cube setSize(float size){
		this.size = size;
		return this;
	}
	
	public Cube setSize(float size, float sizeRand){
		this.size = size;
		this.sizeRand = sizeRand;
		return this;
	}
	
	public Cube setSlide(float cubeSlideSize){
		this.cubeSlideSize = cubeSlideSize;
		return this;
	}
	
	public Cube setSlideRand(float cubeSlideSizeRand, float cubeSlideSizeScale){
		this.cubeSlideSizeRand = cubeSlideSizeRand;
		this.cubeSlideSizeScale = cubeSlideSizeScale;
		return this;
	}
	
	public void draw(float x, float y, float rotation){
		ObjectMap<Integer, float[]> coord = new ObjectMap<>();
		
		float sizeB = size + Mathf.sin(Time.time, sizeScale, sizeRand);
		
		for(int i = 1; i <= 8; i++){
			Vec3 vec = new Vec3(
			size * (Mathf.sign(i <= 4)),
			size * (Mathf.sign(i % 4 == 0 || i % 4 == 3)),
			size * (Mathf.sign(i % 2 == 1))
			).rotate(rotationVec, rotation).rotate(direction, cubeSlideSize + Mathf.absin(Time.time, cubeSlideSizeScale, cubeSlideSizeRand));
			coord.put(i, new float[]{vec.x * sizeB + x, vec.y * sizeB + y, vec.z});
		}
		
		for(int[] indexes : verticesIndex){
			fillQuadByArray(coord, indexes[0], indexes[1], indexes[2], indexes[3]);
		}
		
		Draw.reset();
	}
	
	protected void fillQuadByArray(ObjectMap<Integer, float[]> coord, int index1, int index2, int index3, int index4){
		float z = coord.get(index1)[2] + coord.get(index2)[2] + coord.get(index3)[2] + coord.get(index4)[2];
		float mul = z / size / shade;
		Draw.color(color.cpy().mul(mul * color.r, mul * color.g, mul * color.b, Mathf.num(z > 0)));
		Fill.quad(
			coord.get(index1)[0], coord.get(index1)[1],
			coord.get(index2)[0], coord.get(index2)[1],
			coord.get(index3)[0], coord.get(index3)[1],
			coord.get(index4)[0], coord.get(index4)[1]
		);
	}
}
