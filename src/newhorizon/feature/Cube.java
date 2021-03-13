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
	
	protected final Vec3 directionB = new Vec3(1, 1, 0.5);
	protected final Vec3 directionC = new Vec3(0, 0, -1);
	
	protected final int[][] verticesIndex = {
		{1, 2, 4, 3},
		{5, 1, 3, 7},
		{6, 2, 1, 5},
		{7, 3, 4, 8},
		{8, 4, 2, 6},
		{7, 8, 6, 5},
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
/*
	public void draw(float x, float y, float rotationB, float rotationC){
		//Seq<Vec3> points = new Seq<>(8);
		//Vec3 vecPrimary = new Vec3(1,0,0);
		
		float shadeB = 3;
		float sizeB = 2.7f + Mathf.sin(Time.time, 35, 0.35f);
		float sizeC = Vars.tilesize * 2;
		//float rotationB = Mathf.sin(Time.time, 80, 24);
		//const rotationC = (Mathf.sinDeg(entity.rotation) * 32) + Mathf.sin(Time.time(), 140, 35);
		//float rotationC = Mathf.sin(Time.time, 140, 35);
		
		
		Vec3 directionB = new Vec3(1, 1, 0.5);
		Vec3 directionC = new Vec3(0, 0, -1);
		
		
		Vec3 point1 = new Vec3();
		Vec3 point2 = new Vec3();
		Vec3 point3 = new Vec3();
		Vec3 point4 = new Vec3();
		Vec3 point5 = new Vec3();
		Vec3 point6 = new Vec3();
		Vec3 point7 = new Vec3();
		Vec3 point8 = new Vec3();
		Vec3 point9 = new Vec3();
		
		
		//Vec3 c1z = Mathf.clamp((point1.z + point2.z + point3.z + point4.z) / 4);
		//Vec3 c2z = Mathf.clamp((point1.z + point3.z + point5.z + point7.z) / 4);
		
		//Vec3 b1z = (point1.z + point2.z + point3.z + point4.z) < 0 ? 0 : 1;
		//Vec3 b2z = (point1.z + point3.z + point5.z + point7.z) < 0 ? 0 : 1;
		
		//this set is too complex for me to use Arrays and loops
		
		point1.set(point9.set(sizeC, sizeC, sizeC).rotate(directionC, rotationC)).rotate(directionB, rotationB);
		point2.set(point9.set(sizeC, sizeC, -sizeC).rotate(directionC, rotationC)).rotate(directionB, rotationB);
		point3.set(point9.set(sizeC, -sizeC, sizeC).rotate(directionC, rotationC)).rotate(directionB, rotationB);
		point4.set(point9.set(sizeC, -sizeC, -sizeC).rotate(directionC, rotationC)).rotate(directionB, rotationB);
		point5.set(point9.set(-sizeC, sizeC, sizeC).rotate(directionC, rotationC)).rotate(directionB, rotationB);
		point6.set(point9.set(-sizeC, sizeC, -sizeC).rotate(directionC, rotationC)).rotate(directionB, rotationB);
		point7.set(point9.set(-sizeC, -sizeC, sizeC).rotate(directionC, rotationC)).rotate(directionB, rotationB);
		point8.set(point9.set(-sizeC, -sizeC, -sizeC).rotate(directionC, rotationC)).rotate(directionB, rotationB);
		
		float b1z = (point1.z + point2.z + point3.z + point4.z) < 0 ? 0 : 1;
		float b2z = (point1.z + point3.z + point5.z + point7.z) < 0 ? 0 : 1;
		float b3z = (point6.z + point2.z + point1.z + point5.z) < 0 ? 0 : 1;
		float b4z = (point7.z + point3.z + point4.z + point8.z) < 0 ? 0 : 1;
		float b5z = (point8.z + point4.z + point2.z + point6.z) < 0 ? 0 : 1;
		float b6z = (point7.z + point8.z + point6.z + point5.z) < 0 ? 0 : 1;
		
		float c1z = Mathf.clamp((point1.z + point2.z + point3.z + point4.z) / sizeC / shadeB);
		float c2z = Mathf.clamp((point1.z + point3.z + point5.z + point7.z) / sizeC / shadeB);
		float c3z = Mathf.clamp((point6.z + point2.z + point1.z + point5.z) / sizeC / shadeB);
		float c4z = Mathf.clamp((point7.z + point3.z + point4.z + point8.z) / sizeC / shadeB);
		float c5z = Mathf.clamp((point8.z + point4.z + point2.z + point6.z) / sizeC / shadeB);
		float c6z = Mathf.clamp((point7.z + point8.z + point6.z + point5.z) / sizeC / shadeB);
		
		float b1x = (point1.x * sizeB) + x;
		float b1y = (point1.y * sizeB) + y;
		float b2x = (point2.x * sizeB) + x;
		float b2y = (point2.y * sizeB) + y;
		float b3x = (point3.x * sizeB) + x;
		float b3y = (point3.y * sizeB) + y;
		float b4x = (point4.x * sizeB) + x;
		float b4y = (point4.y * sizeB) + y;
		float b5x = (point5.x * sizeB) + x;
		float b5y = (point5.y * sizeB) + y;
		float b6x = (point6.x * sizeB) + x;
		float b6y = (point6.y * sizeB) + y;
		float b7x = (point7.x * sizeB) + x;
		float b7y = (point7.y * sizeB) + y;
		float b8x = (point8.x * sizeB) + x;
		float b8y = (point8.y * sizeB) + y;
		
		Color color = NHColor.lightSky;
		
		Draw.color(color.cpy().mul(c1z, c1z, 1, b1z));
		Fill.quad(b1x, b1y, b2x, b2y, b4x, b4y, b3x, b3y);
		Draw.color(color.cpy().mul(c2z, c2z, 1, b2z));
		Fill.quad(b5x, b5y, b1x, b1y, b3x, b3y, b7x, b7y);
		Draw.color(color.cpy().mul(c3z, c3z, 1, b3z));
		Fill.quad(b6x, b6y, b2x, b2y, b1x, b1y, b5x, b5y);
		Draw.color(color.cpy().mul(c4z, c4z, 1, b4z));
		Fill.quad(b7x, b7y, b3x, b3y, b4x, b4y, b8x, b8y);
		Draw.color(color.cpy().mul(c5z, c5z, 1, b5z));
		Fill.quad(b8x, b8y, b4x, b4y, b2x, b2y, b6x, b6y);
		Draw.color(color.cpy().mul(c6z, c6z, 1, b6z));
		Fill.quad(b7x, b7y, b8x, b8y, b6x, b6y, b5x, b5y);
		
		Draw.reset();
	}
*/
	
	public void draw(float x, float y, float rotation){
		ObjectMap<Integer, float[]> coord = new ObjectMap<>();
		
		float sizeB = size + Mathf.sin(Time.time, sizeScale, sizeRand);
		
		for(int i = 1; i <= 8; i++){
			Vec3 vec = new Vec3(
			size * (Mathf.sign(i <= 4)),
			size * (Mathf.sign(i % 4 == 0 || i % 4 == 3)),
			size * (Mathf.sign(i % 2 == 1))
			).rotate(directionC, rotation).rotate(directionB, cubeSlideSize + Mathf.absin(Time.time, cubeSlideSizeScale, cubeSlideSizeRand));
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
