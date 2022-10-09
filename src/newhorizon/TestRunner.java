package newhorizon;

public class TestRunner{
//	public static void main(String[] args){
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
