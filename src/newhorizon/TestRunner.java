package newhorizon;

public class TestRunner{
	public static void main(String[] args){
		int w = 240, h = 240;
		int cx = w / 2, cy = h / 2;
		
		int[] inputX = {72, 149, 163, 160, 99, 140};
		int[] inputY = {164, 176, 133, 205, 207, 207};
		
		for(int i = 0; i < inputX.length; i++){
			int py = h - inputY[i];
			int px = inputX[i];
			
			int dx = px - cx;
			int dy = py - cy;
			
			System.out.println("X: " + dx / 4f + "f");
			System.out.println("Y: " + dy / 4f + "f");
			System.out.println("-------------");
		}
		
		int i;
		for(i = 0; i < 5; i++){
			System.out.println(i);
		}
	}
		
//		System.out.println("\uf15c\uf15b\uf0f6\ue802\ue803\ue804\ue805\ue807\ue800\ue808\ue809\ue80b\ue80f\uf300\uf1c5\ue813\ue816\ue819\ue81a\uf0b0\ue81d\ue822\ue824\ue825\ue826\ue827\ue823\ue829\ue806\ue811\ue815\ue818\uf120\ue835\ue836\uf129\ue837\ue839\ue83a\ue83b\ue83e\ue83f\uf12d\ue801\uf029\ue812\ue842\ue844\ue80d\ue81e\uf281\uf308\ue83d\ue845\uf181\ue80e\ue814\ue817\ue81b\ue81c\ue82a\ue82b\ue82c\ue82d\ue830\ue84c\ue852\ue853\ue85b\ue85c\ue85d\ue85e\ue85f\ue861\ue865\ue867\ue868\ue869\ue86a\ue86b\ue86c\ue86d\ue86e\ue86f\ue870\ue871\ue872\ue873\ue874\ue875\ue876\ue877\ue878\ue879\ue87b\ue87c\ue87d\ue88a\ue88b\ue810\ue88c\ue88d\ue88e\ue88f⚠\ue864\ue84d\ue833");
//		print(System.getenv("MINDUSTRY_DEBUG"));
//
//		try{
//			File debugLog = new File("E:/Java_Projects/MDT_Mod_Project/NewHorizonMod/build/libs/debug.properties");
//			if(!debugLog.exists())//noinspection ResultOfMethodCallIgnored
//				debugLog.createNewFile();
//
//			Reader fileReader = new FileReader(debugLog);
//			Properties property = new Properties();
//			property.load(fileReader);
//			long toEpochMilli = Long.parseLong(String.valueOf(property.get("lastInstant_toEpochMilli")));
//			fileReader.close();
//
//			Instant last = Instant.ofEpochMilli(toEpochMilli);
//
//			Process proc = Runtime.getRuntime().exec("tasklist");
//			BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(proc.getInputStream()), StandardCharsets.UTF_8));
//
//			List<ProcessHandle> handles = new ArrayList<>();
//			List<Long> detected = new ArrayList<>();
//
//			String str;
//			while((str = reader.readLine()) != null){
//				if(str.contains("java.exe"))detected.add(Long.parseLong(str.substring(25, 35).trim()));
//			}
//
//			ProcessHandle.allProcesses().forEach(p -> {
//				if(detected.remove(p.pid())){
//					if(p.info().startInstant().isPresent() && last.equals(p.info().startInstant().get())){
//						p.destroy();
//					}
//				}
//			});
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//	}
//
//	public static void print(Object o){
//		System.out.println(o);
//	}
}
