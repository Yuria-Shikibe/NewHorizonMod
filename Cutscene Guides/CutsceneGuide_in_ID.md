# Panduan Adegan Kostum 

**Digunakan untuk membuat skrip animasi peta.** 

## KATA PEMBUKA 

- Kenapa translasi panduan ini ada meskipun kamu harus paham inggris untuk paham javascript sepenuhnya? ~~gak tau~~ 
- Walaupun skrip adegan berkaitan dengan javascript, ini sebenarnya tidak terlalu sulit.
- Tentu saja, kamu perlu beberapa gramatika inggris untuk membuat perjalananmu lebih mudah, tetapi skrip adegan seperti balok Lego. Kamu perlu menyangga blok kode satu persatu, kemudian kamu bisa membuat adegan sendiri.
- Jadi santai saja. **_Kamu pasti bisa_**
- Contoh: [Skrip untuk peta (@HC)Hostile HQ](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/assets/custom-cutscene/(%40HC)Hostile%20HQ-cutscene.js)
- Kode Utama: [CutsceneScript.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/feature/CutsceneScript.java) 

--- 

### Fitur 

- Powered by **JavaScript**.
- Bisa disimpan ke file peta setelah kamu menyelesaikannya, membuatnya bisa digunakan tanpa *file external*.
- Tersedia untuk permainan multi pemain dan server.
- Mempunyai debugger di dalam game. Kamu perlu mengaktifkan pengaturan mod berikut untuk mengaksesnya.
  - `Mode Debug`
  - `Panel Alat` 

### Perhatian 

- Tidak aman karena js tidak punya **_SECURITY LIMIT_**.
- Susah untuk di debug, karena crashes sering terjadi tanpa sebuah laporan, dan alatnya masih dalam tahap perkembangan saat ini.
- Meskipun peta memiliki kedua skrip terkemas & file skrip external, hanya yang terkemas yang dijalankan. 

## CARA MENGGUNAKAN 

### PRE IMPORTER 

```js
Log.info("Loaded Cutscene Vault"); 

const loader = Vars.mods.getMod(modName).loader; 

const loadClass = (fullName) => loader.loadClass(fullName).newInstance(); //Garbage things 

const CutsceneScript = loadClass("newhorizon.util.feature.cutscene.CutsceneScript");
const UIActions = loadClass("newhorizon.util.feature.cutscene.CutsceneScript$UIActions");
const KeyFormat = loadClass("newhorizon.util.feature.cutscene.CutsceneScript$KeyFormat");
const WorldActions = loadClass("newhorizon.util.feature.cutscene.CutsceneScript$WorldActions"); 

const NHBlocks = loadClass("newhorizon.content.blocks.NHBlocks");
const NHBullets = loadClass("newhorizon.content.NHBullets");
const NHItems = loadClass("newhorizon.content.NHItems");
const NHLiquids = loadClass("newhorizon.content.NHLiquids");
const NHSounds = loadClass("newhorizon.content.NHSounds");
const NHWeathers = loadClass("newhorizon.content.NHWeathers");
const NHUnitTypes = loadClass("newhorizon.content.NHUnitTypes");
const NHStatusEffects = loadClass("newhorizon.content.NHStatusEffects");
const NHSectorPresets = loadClass("newhorizon.content.NHSectorPresets");
const NHFx = loadClass("newhorizon.content.NHFx");
const NHColor = loadClass("newhorizon.content.NHColor");
const NHPlanets = loadClass("newhorizon.content.NHPlanets");
const NHFunc = loadClass("newhorizon.util.func.NHFunc");
const DrawFunc = loadClass("newhorizon.util.graphic.DrawFunc");
const Tables = loadClass("newhorizon.util.ui.NHUIFunc");
const TableFunc = loadClass("newhorizon.util.ui.TableFunc");
const NHInterp = loadClass("newhorizon.util.func.NHInterp");
const PosLightning = loadClass("newhorizon.util.feature.PosLightning"); 

const OFFSET = 12, LEN = 60; 

const state = Vars.state;
const tilesize = Vars.tilesize;
const world = Vars.world;
``` 

Metode yang saya gunakan untuk mengimpor `Class` sangat buruk. Jika kamu mampu membuatnya lebih baik, lakukan **PULL REQUEST** .
Dan juga, ketika js adegan mu sedang dijalankan oleh mod, baris kode dipluskan nomor baris kode PRE IMPORTER.
> Contoh: 
> 
> Kamu menulis ```WorldActions.raidFromCoreDefault(NHBulltes.airRaid, 10, 1, 120, 1)``` di baris pertama file skrip kamu.
> 
> Jika suatu kesalahan terjadi di baris ini, laporannya akan bilang `Line 36(35 + 1)` mempunyai masalah 

Importer ini telah mengimpor sebagian dari Class MOD yang akan kamu perlukan. Jika kamu perlu lebih, kamu bisa memanggil metode `loadClass(<String> Class Full Name);` untuk memuat lebih banyak class. 

### Proses Utama 

1. Tulis `(@HC)`, Yang berarti `Annotation: Has Cutscene`, ke nama petamu.
> Seperti ini
> ![](https://github.com/Yuria-Shikibe/NewHorizonMod/raw/main/github-pictures/guide/cutscene-guide-rename.png)
2. Simpan petanya dan buka.
3. File js dengan nama spesifik akan dibuat secara otomatis. Jika semua berjalan dengan lancar, tekan `F8` untuk membuka `last-log` dan kamu akan melihat ini
: ![](https://github.com/Yuria-Shikibe/NewHorizonMod/raw/main/github-pictures/guide/cutscene-js-auto-generated.png)
4. Tulis skrip kamu di file yang dibuat oleh mod ini.
5. Pergi ke waktu debugging susah.
6. Buka `Menu` di `Editor Peta` (Tekan `ESC` di keyboard kamu atau `Tombol Pulang` di ponselmu, atau klik tombol dialog diatas kiri), Klik tombol `Cutscene Scripts`: ![](https://github.com/Yuria-Shikibe/NewHorizonMod/raw/main/github-pictures/guide/cutscene-guide-package1.png)
7. Klik tombol `Package Scripts`, kemudian pilih file js yang sesuai dan konfirmasi.
8. Jika kamu ingin konfirmasi, Tekan tombol `Read Scripts` untuk mengetahui jika peta yang memiliki skrip kamu terinstall atau tidak.
9. Simpan dan bukan petanya, coba skrip kamu.
10. Jika suatu kesalahan terjadi yang tidak muncul sebagai terjadi, coba untuk mengemas ulang skripnya.
11. Tag `(@HC)` bisa dihapus setelah kamu selesai debugging, atau kamu bisa menyimpannya sebagai tanda yang memberitahu orang peta ini memiliki adegan.
12. Bagikan map kamu. 

### Metode Inti
```java
public static boolean actionSeq(Action... actions){
    boolean isPlaying = isPlayingCutscene;
    
    Action[] acts = new Action[actions.length + 1];
    System.arraycopy(actions, 0, acts, 0, actions.length);
    acts[acts.length - 1] = Actions.parallel(Actions.remove(), Actions.run(() -> currentActions = null));
    
    if(!isPlaying){
        isPlayingCutscene = true;
        currentActions = acts;
        Table filler = new Table(Tex.clear){
            {
                Core.scene.root.addChild(this);
                
                setFillParent(true);
                visible(UIActions::shown);
                
                keyDown(k -> {
                    if(k == KeyCode.escape) remove();
                });
            }
            
            @Override
            public void act(float delta){
                super.act(delta);
                if(Vars.state.isMenu()) remove();
            }
            
            @Override
            public boolean remove(){
                enableVanillaUI();
                
                if(waitingPool.any()){
                    Time.run(60f, () -> {
                        isPlayingCutscene = false;
                        actionSeq(waitingPool.pop());
                    });
                }else isPlayingCutscene = false;
                
                return super.remove();
            }
        };
        
        filler.actions(acts);
    }else{
        waitingPool.add(acts);
    }
    
    return isPlaying;
}
``` 

- Isi metodenya dengan `Action` satu persatu, ikuti urutan waktu.
- Adegan dijalankan oleh `arc.scene.Action`.
- Jika kamu keluar dari dunia selagi adegan masih berjalan, Dunia tidak akan disimpan, alhasil menghasilkan beberapa masalah penyimpanan.
- Jika beberapa action dijalankan secara menumpuk, mereka akan dilaksanakan satu persatu. 

#### Perhatian
- Semua waktu `Action` diformatkan menjadi **Second** Selagi format metode yang lain adalah `tick(1 / 60 Sec)`.
- Hampir semua metode menggunakan koordinat **\*8**. 

### Bidang & Metode Yang Digunakan Secara Umum 

#### Interpolasi: `Interp` & `NHInterp` 

![](https://github.com/Yuria-Shikibe/NewHorizonMod/raw/main/github-pictures/guide/interps.png)
- Kamu bisa mendapatkan ini semua dari `arc.math.Interp`, `newhorizon.util.func.NHInterp`.
- Ini semua digunakan untuk kurva animasi, yang dimana ini semua bisa diatur.
- Jika kamu mengaktifkan `Panel Alat` & `Mode Debug` di `Pengaturan Mod`, kamu bisa meakses tabel ini dari *Tabel Cheat -> Debug -> Interp*.
--- 

#### Action: `arc.scene.Action` 

- Asalnya digunakan untuk UI animasi. Sekarang beberapa dari mereka bisa digunakan di skrip adegan.
  - `DelayAction`
  - `ParallelAction`
  - `SequenceAction`
  - `RunnableAction`
  - `ImportantRunnableActi`
  - `LabelAction`
  - `CameraMoveAction`
  - `CameraTrackerAction`
  - `CautionAction`
  - `AddAction`
  - `AddListenerAction`
  - `RemoveListenerAction`
  - `AfterAction`
  - `IntAction`
  - `FloatAction`
  - `TimeScaleAction`
  - `RepeatAction` 

--- 

#### Bidang & Metode Dari Class: `CutsceneScript` 

##### Gudang Aktor 

```java
public static final Seq<Runnable> curUpdater = new Seq<>(), curIniter = new Seq<>();
public static final Seq<Cons<Boolean>> curEnder = new Seq<>();
``` 

- `curUpdater` Digunakan untuk menyimpan pergerakan yang beraksi setiap update(Jangan dijalankan selama berhenti).
- `curIniter` Digunakan untuk menyimpan pergerakan yang beraksi ketika dunia dimuat.
- `curEnder` Digunakan untuk menyimpan pergerakan yang beraksi setiap game berakhir. Param `Boolean`: true -> win; false -> lose. 

--- 

##### Pendengar Blok Hancur 

```java
public static final ObjectMap<Block, Cons<Building>> blockDestroyListener = new ObjectMap<>();
``` 

- Digunakan untuk menyimpan pergerakan yang memanggil tipe blok hancur yang spesifik 

--- 

##### Pewaktu 

```java
public static Interval timer = new Interval(6);
``` 

- Digunakan untuk peristiwa yang mempunyai jeda pendek.
- Gunakan *Metode:* `reload(...)` untuk peristiwa yang mempunyai jeda panjang dan perlu disimpan. 

--- 

##### Metode: 

###### addListener(Seq<Block> types, Cons<Building> actor)
```java
public static void addListener(Seq\<Block> types, Cons<Building> actor){
    for(Block type : types)addListener(type, actor);
}
```
- Digunakan untuk menambahkan `Pendengar Blok Hancur` untuk beberapa tipe blok dalam sekaligus 

###### canInit()
```java
public static boolean canInit(){
    boolean b = !state.rules.tags.containsKey("inited") || !Boolean.parseBoolean(state.rules.tags.get("inited"));
    state.rules.tags.put("inited", "true");
    initHasRun = true;
    return b;
}
```
- Digunakan untuk mencek jika mod ini tidak menjalankan init adegan.
- Menggunakan `if` statement, dan menulis inisiasi action di statement berikut 

###### eventHasData(String key)
```java
public static boolean eventHasData(String key){
    return state.rules.tags.containsKey(key);
}
```
- Digunakan untuk mencek jika sebuah peristiwa memiliki data atau tidak.
- Gunakan ini untuk mengetahui jika sebuah peristiwa terjadi atau akan . 

###### run(String key, Boolf\<String> boolf, Runnable run)
```java
public static void run(String key, Boolf<String> boolf, Runnable run){
    if(state.rules.tags.containsKey(key) && boolf.get(state.rules.tags.get(key))){
        run.run();
    }
}
```
- Digunakan untuk menjalankan sebuah peristiwa ketika data peristiwa dikualifikasi. 

###### getBool(String key)
```java
public static boolean getBool(String key){
    return state.rules.tags.containsKey(key) && Boolean.parseBoolean(state.rules.tags.get(key));
}
```
- Digunakan untuk mendapatkan `true` jika data peristiwa sama dengan `"true"`, atau ia akan memberikan `false`. 

###### getFloat(String key)
```java
public static float getFloat(String key){
    return Float.parseFloat(state.rules.tags.get(key));
}
```
- Digunakan untuk mendapatkan `float` jika data peristiwa di string tidak mengandung sebuah `float` yang dapat diuraikan, atau ia akan memberikan `Exception`. 

###### getFloatOrNaN(String key)
```java
public static float getFloatOrNaN(String key){
    float f = Float.NaN;
    try{
        f = Float.parseFloat(state.rules.tags.get(key));
    }catch(Exception ignore){}
    return f;
}
```
- Digunakan untuk mendapatkan `float` jika sebuah data peristiwa di string tidak mengandung sebuah `float` yang dapat diuraikan, atau ia akan memberikan `Float.NaN`. 

~~Maaf, saya kelelahan, Saya mungkin akan menyelesaikan ini nanti. Baca dulu [CutsceneScript.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/feature/CutsceneScript.java)~~
