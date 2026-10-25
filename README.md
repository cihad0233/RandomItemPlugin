# RandomItemPlugin (Minecraft 1.8.9 / Spigot)

Anlattığın oyun akışına göre hazırlanmış bir "Random Item" (son hayatta kalan kazanır) eklentisi.

## Neler var?

- Sınırsız sayıda harita (arena) oluşturma, her haritada 5 oyunculuk kapasite.
- 5 doğma noktasını komutla tek tek ayarlama (`/ri setspawn <harita> 1..5`).
- Bir NPC'ye (Armor Stand) sağ tıklayınca açılan harita menüsü:
  - Oyun oynanıyorsa kırmızı görünür ve tıklanamaz, "Oyun oynaniyor, girilemez!" yazar.
  - Boşsa yeşil görünür, "Oyuncular: 2/5" gibi lore gösterir, tıklayınca otomatik katılırsın.
- Harita dolunca (5/5) 5 saniye geri sayımla oyun otomatik başlar; `/ri forcestart` ile elle de başlatılabilir.
- Oyun sırasında ~4 saniyede bir tüm canlı oyunculara rastgele eşya dağıtılır (kılıç, zırh, altın elma, TNT, ok, iksir vb. — liste `RandomItemTask.java` içinde kolayca değiştirilebilir).
- Bir oyuncu ölünce elenir, izleyici moduna geçer; son kalan oyuncu kazanır ve oyun otomatik biter.

### Menü boyutu hakkında not
Minecraft'ta gerçek bir "9x9" envanter yok (bir sandık en fazla 9x6 = 54 slot olabilir). Bu yüzden harita menüsü 54 slotluk (6 satır, her satırda 9 harita) bir GUI olarak yapıldı — "menü" ihtiyacını tam karşılıyor, sadece boyut ismi farklı.

## Kurulum

1. **Dünyayı hazırla:** Multiverse-Core + bir void/boş dünya generator eklentisi (VoidGen vb.) ile boş bir dünya oluştur, örn: `/mv create oyunharita normal -g VoidGenerator`.
2. **Bedrock daireleri:** Oyun içinde 5 adet bedrock'tan oluşan yuvarlak platform inşa et (örn. WorldEdit ile `//cyl` komutuyla kolayca daire yapabilirsin).
3. Bu eklentiyi derleyip (aşağıya bak) sunucunun `plugins` klasörüne at, sunucuyu başlat/yeniden başlat.
4. Haritayı tanımla:
   ```
   /ri create Arena1
   ```
5. Her bedrock platformunun üzerine tek tek çık ve sırasıyla:
   ```
   /ri setspawn Arena1 1
   /ri setspawn Arena1 2
   /ri setspawn Arena1 3
   /ri setspawn Arena1 4
   /ri setspawn Arena1 5
   ```
6. Menü NPC'sini istediğin lobi noktasında oluştur:
   ```
   /ri setnpc Arena1
   ```
   (Not: Her harita için ayrı NPC koymana gerek yok; NPC'ye tıklandığında tüm haritaları listeleyen aynı menü açılır. Tek bir ana lobi NPC'si yeterli — birden fazla koyarsan hepsi aynı menüyü açar.)
7. `/ri list` ile haritaları kontrol et, `/ri menu` ile menüyü test et.

## Komutlar

| Komut | Yetki | Açıklama |
|---|---|---|
| `/ri create <isim>` | randomitem.admin | Yeni harita oluşturur, otomatik "seçili harita" yapar |
| `/ri select <isim>` | randomitem.admin | Bir haritayı "seçili" yapar (sonraki komutlarda ismi tekrar yazmaya gerek kalmaz) |
| `/ri delete <isim>` | randomitem.admin | Haritayı siler |
| `/ri setspawn <numara>` | randomitem.admin | Seçili haritaya o anki konumu doğma noktası yapar |
| `/ri setspawn <isim> <numara>` | randomitem.admin | Belirli bir haritaya doğma noktası ekler |
| `/ri setnpc [isim]` | randomitem.admin | Bulunduğun yere menü NPC'si koyar (isim verilmezse seçili harita) |
| `/ri setlobby` | randomitem.admin | Oyun bitince oyuncuların ışınlanacağı lobi noktasını ayarlar |
| `/ri setspectator` | randomitem.admin | Ölen oyuncuların izleyici modunda bekleyeceği noktayı ayarlar |
| `/ri list` | - | Haritaları ve durumlarını listeler |
| `/ri menu` | - | Harita menüsünü açar |
| `/ri forcestart <isim>` | randomitem.admin | Oyunu elle başlatır (spawn'lar eksikse reddeder) |
| `/ri leave` | - | Oyundan ayrılır |

## config.yml ile ayarlanabilenler

Eklenti ilk açıldığında `plugins/RandomItemPlugin/config.yml` dosyası otomatik oluşur. Düzenleyip `/reload` yaptığında (ya da sunucuyu yeniden başlattığında) devreye girer:

- `max-players`: Yeni oluşturulacak haritalardaki oyuncu/doğma noktası sayısı (varsayılan 4)
- `item-interval-seconds`: Oyun sırasında kaç saniyede bir rastgele eşya verileceği (varsayılan 15)
- `menu-start-slot`: Harita menüsünde ilk haritanın yerleşeceği slot (0-53, varsayılan 20)
- `placeable-weight`: "placeable" listesindeki (inşaat) blokların diğer eşyalara göre kaç kat daha sık geleceği
- `items.placeable` / `items.other`: Rastgele dağıtılacak eşya listeleri (1.8 Material isimleriyle)
- `lobby` / `spectator-spawn`: `/ri setlobby` ve `/ri setspectator` ile otomatik yazılır, elle dokunmana gerek yok

## .jar dosyasını hazır olarak alma (önerilen, kurulum gerektirmez)

Bu sandbox ortamının ağ erişimi sadece belirli adreslerle (GitHub, npm, pypi vb.) sınırlı; Spigot'un kendi paket deposu (`hub.spigotmc.org`) bu listede olmadığı için jar'ı burada derleyemedim. Bunun yerine projeye **otomatik derleme yapan bir GitHub Actions workflow'u** (`.github/workflows/build.yml`) ekledim — bilgisayarına hiçbir şey kurmadan hazır .jar almak için:

1. GitHub'da yeni (boş) bir repo oluştur.
2. Bu zip'in içindekileri o reponun içine push'la (GitHub'ın web arayüzünden "Add file > Upload files" ile de sürükleyip bırakabilirsin).
3. Repoya girince üstte **Actions** sekmesine tıkla, "Build plugin jar" workflow'unu göreceksin (push sonrası otomatik de çalışır).
4. Çalışma bitince (birkaç dakika sürer) workflow'un sayfasında **Artifacts** kısmından `RandomItemPlugin-jar` dosyasını indir, içinden çıkan `RandomItemPlugin.jar`'ı sunucunun `plugins` klasörüne at.

## Derleme (Maven ile kendi bilgisayarında .jar üretme)

Kendi bilgisayarında derlemek istersen:

1. Java 8+ ve Maven kurulu olsun.
2. Spigot 1.8.8 API'sini local repona kazandırmak için [BuildTools](https://www.spigotmc.org/wiki/buildtools/) ile `java -jar BuildTools.jar --rev 1.8.8` çalıştır (bu, spigot-api'yi local Maven deponuza yükler).
3. Proje klasöründe:
   ```
   mvn clean package
   ```
4. Oluşan `target/RandomItemPlugin.jar` dosyasını sunucunun `plugins` klasörüne koy.

## Genişletme fikirleri

- Rastgele eşya havuzunu (`RandomItemTask.ITEM_POOL`) kendi zevkine göre değiştir.
- Oyun başlamadan önce ekstra bir geri sayım/hazırlık süresi (kafes/bariyer) eklenebilir.
- Kazanana ödül (para, kill sayacı vb.) vermek için `GameController.checkWinner` içine ekleme yapılabilir.
