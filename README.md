# NOVA SHOP — Android starter

Ovo je početni Android Studio projekt za demo trgovinu NOVA SHOP.

## Pokretanje
1. Instalirajte Android Studio (preporučeno najnovije stabilno izdanje).
2. Otvorite mapu `nova_shop_android` kao postojeći projekt.
3. Pričekajte Gradle Sync.
4. Pokrenite aplikaciju na emulatoru ili Android uređaju.

## Što radi u ovoj demo verziji
- početni ekran i promotivni banner
- kategorije i pretraživanje
- katalog demo proizvoda
- dodavanje i promjena količina u košarici
- demo tijek narudžbe
- administratorski ekran za dodavanje/brisanje demo proizvoda
- lokalno spremanje kataloga i košarice na uređaju (ostaje nakon zatvaranja aplikacije)

## Važno — što još nije produkcijski spojeno
Ovo nije objavljena aplikacija niti kompletna produkcijska trgovina. Podaci se u ovoj međufazi spremaju lokalno na uređaj; još nisu sinkronizirani između uređaja niti spremljeni na poslužitelju. Za stvarni rad potrebno je dodati:
- backend API i bazu podataka (npr. Supabase ili Firebase)
- sigurnu registraciju/prijavu i role-based administratorske ovlasti
- stvarno spremanje proizvoda, zaliha, kupaca i narudžbi
- adresar i izračun dostave
- pouzdanog pružatelja plaćanja (npr. Stripe ili lokalni PSP), bez pohrane kartičnih podataka u aplikaciji
- push obavijesti, pravila privatnosti, uvjete kupnje i GDPR procese
- testiranje, potpisivanje i objavu na Google Playu

Nikada ne stavljajte administratorske lozinke ili tajne API ključeve u Android aplikaciju.

## Što je dodano u ovoj međufazi
- proizvodi (uključujući promjene u demo administraciji) i košarica spremaju se u lokalnu pohranu Android aplikacije
- podaci se učitavaju pri sljedećem pokretanju aplikacije

## Sljedeći produkcijski korak
Za sinkronizaciju s više uređaja i sigurnu administratorsku prijavu treba otvoriti projekt na backend platformi (npr. Supabase) i postaviti podatke projekta. Ne unositi tajne ključeve u aplikaciju.


## Izrada APK-a bez vlastitog računala (GitHub Actions)
Ovaj ZIP sadrži workflow `.github/workflows/android.yml` za izgradnju probnog APK-a u GitHubovom cloudu.

1. Na mobitelu otvori https://github.com i napravi besplatan račun ili se prijavi.
2. Kreiraj novi **Public** ili **Private** repository, npr. `nova-shop-android`.
3. Raspakiraj ovaj ZIP na mobitelu. U repozitorij treba prenijeti SADRŽAJ mape `nova_shop_android` (uključujući skrivenu mapu `.github`), a ne ZIP datoteku.
4. Na GitHubu otvori **Add file → Upload files** i prenesi sve datoteke i mape projekta. Ako mobilni preglednik ne dopušta prijenos cijele mape, uključi prikaz za računalo ili koristi GitHubovu aplikaciju/preglednik koji podržava prijenos više datoteka.
5. Pričekaj da prijenos završi. Workflow će se pokrenuti nakon spremanja na granu `main`.
6. Otvori karticu **Actions**, odaberi uspješan posao **Build NOVA SHOP APK**, pa u dijelu **Artifacts** preuzmi `nova-shop-debug-apk`.
7. Raspakiraj preuzeti artifact i instaliraj `app-debug.apk`. Android može tražiti dopuštenje za instalaciju aplikacija iz tog izvora.

Napomene:
- GitHub račun i korištenje Actions imaju vlastite uvjete i kvote; ne dijeli lozinke ili pristupne tokene.
- Ovo je nepotpisani debug APK za testiranje, ne za Google Play ili javnu prodaju.
- APK će sadržavati samo trenutačne demo funkcije i lokalnu pohranu. Nema stvarnog backend sustava, sigurne administratorske prijave ni stvarnog plaćanja.
- Ako build ne uspije, otvori neuspjeli job u Actions i pošalji mi tekst greške ili snimku zaslona.
