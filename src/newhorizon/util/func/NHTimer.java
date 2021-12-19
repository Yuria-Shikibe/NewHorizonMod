//package newhorizon.util.func;
//
//import arc.struct.Seq;
//import arc.util.Time;
//import arc.util.pooling.Pools;
//
//public class NHTimer{
//	private static Seq<Time.DelayRun> runs = new Seq<>();
//	private static Seq<Time.DelayRun> removal = new Seq<>();
//
//	public static void run(float delay, Runnable r){
//		Time.DelayRun run = Pools.obtain(Time.DelayRun.class, Time.DelayRun::new);
//		run.finish = r;
//		run.delay = delay;
//		runs.add(run);
//	}
//
//	public static void update(){
//		timeRaw += delta;
//		removal.clear();
//
//		if(Double.isInfinite(timeRaw) || Double.isNaN(timeRaw)){
//			timeRaw = 0;
//		}
//
//		time = (float)timeRaw;
//		globalTime = (float)globalTimeRaw;
//
//		for(Time.DelayRun run : runs){
//			run.delay -= delta;
//
//			if(run.delay <= 0){
//				run.finish.run();
//				removal.add(run);
//				Pools.free(run);
//			}
//		}
//
//		runs.removeAll(removal);
//	}
//
//	static{
//		Time
//	}
//}
