package it.agilelab.bigdata.gis.domain.loader

import com.typesafe.config.{Config, ConfigFactory}
import it.agilelab.bigdata.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.bigdata.gis.domain.managers.OSMManager
import it.agilelab.bigdata.gis.domain.models.ReverseGeocodingResponse
import org.scalatest.{BeforeAndAfterAll, EitherValues, FlatSpec, Matchers}

class OSMManagerSpec extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  val conf: Config = ConfigFactory.load()
  val osmConf: Config = conf.getConfig("osm")
  val osmManager: OSMManager = OSMManager(osmConf)

  "Reverse geocoding on Andorra" should "work" in {
    val id = "abc"
    val point = IdentifiableGPSPoint(id, 42.542703, 1.515542, None, System.currentTimeMillis())
    val randomPlaceInAndorraActual: ReverseGeocodingResponse = osmManager.reverseGeocode(point).right.value

    val randomPlaceInAndorraExpected: ReverseGeocodingResponse =
      ReverseGeocodingResponse(
        id,
        street = Some(""),
        city = Some("La Massana"),
        county = None,
        countyCode = None,
        region = None,
        country = Some("Andorra"),
        countryCode = Some("AD"), // as per https://www.iso.org/obp/ui/#iso:code:3166:AD
        postalIndex = None,
        addressRange = None,
        speedLimit = None,
        speedCategory = None,
        roadType = Some("residential"),
        distance = Some(12.646825786646783)
      )

    randomPlaceInAndorraActual should be(randomPlaceInAndorraExpected)
  }

  "Reverse geocoding on Italy" should "work" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 45.068032, 7.643780, None, System.currentTimeMillis())

    val viaAzziActual: ReverseGeocodingResponse = osmManager.reverseGeocode(point).right.value

    val viaAzziExpected: ReverseGeocodingResponse =
      ReverseGeocodingResponse(
        id,
        street = Some("Via Francesco Azzi"),
        city = Some("Turin"),
        county = Some("Torino"),
        countyCode = Some("TO"),
        region = Some("Piemont"),
        country = Some("Italy"),
        countryCode = Some("IT"), // as per https://www.iso.org/obp/ui/#iso:code:3166:IT
        postalIndex = Some("10024"),
        addressRange = Some("10"),
        speedLimit = Some(50),
        speedCategory = None,
        roadType = Some("unclassified"),
        distance = Some(8.934272840344661)
      )

    viaAzziActual should be(viaAzziExpected)
  }

  "Reverse geocoding on Italy" should "has correct postalcode valued" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 45.3395, 11.8863, None, System.currentTimeMillis())

    val albignasego: ReverseGeocodingResponse = osmManager.reverseGeocode(point).right.value

    val albignasegoExptected: ReverseGeocodingResponse =
      ReverseGeocodingResponse(
        id,
        street = None,
        city = Some("Albignasego"),
        county = Some("Padova"),
        countyCode = Some("PD"),
        region = Some("Veneto"),
        country = Some("Italy"),
        countryCode = Some("IT"), // as per https://www.iso.org/obp/ui/#iso:code:3166:IT
        postalIndex = Some("35020"),
        addressRange = None,
        speedLimit = None,
        speedCategory = None,
        roadType = None,
        distance = None
      )

    albignasego should be(albignasegoExptected)
  }

  "Reverse geocoding on Italy" should "has correct house number valued" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 45.08333, 7.61496, None, System.currentTimeMillis())

    val corsoSaccoEVanzettiActual: ReverseGeocodingResponse = osmManager.reverseGeocode(point).right.value

    val corsoSaccoEVanzettiExpected: ReverseGeocodingResponse =
      ReverseGeocodingResponse(
        id,
        street = Some("Corso Sacco e Vanzetti"),
        city = Some("Turin"),
        county = Some("Torino"),
        countyCode = Some("TO"),
        region = Some("Piemont"),
        country = Some("Italy"),
        countryCode = Some("IT"),
        postalIndex = Some("10024"),
        addressRange = Some("9 scala A"),
        speedLimit = None,
        speedCategory = None,
        roadType = Some("residential"),
        distance = Some(1.4465500367107154)
      )

    corsoSaccoEVanzettiActual should be(corsoSaccoEVanzettiExpected)
  }


  /*-------------------------------*/
  /*LOAD MAP TO REMOVE THIS COMMENT*/
  /*-------------------------------*/
  //  "Reverse geocoding on Belgium" should "work" in {
  //    val lat = 50.833954
  //    val lon = 4.301101
  //
  //    val randomPlaceInAndrelecthActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInAndrelecthExpected: Address =
  //      Address(
  //        Some("Avenue Victor et Jules Bertaux - Victor en Jules Bertauxlaan"),
  //        Some("Anderlecht"),
  //        None,
  //        Some("Brussels-Capital"),
  //        Some("Belgium"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("residential"),
  //        Some(6.630406557226943)
  //      )
  //
  //    randomPlaceInAndrelecthActual should be (randomPlaceInAndrelecthExpected)
  //  }

  //  "Reverse geocoding on Bosnia" should "work" in {
  //    val lat = 43.861599
  //    val lon = 18.365856
  //    val randomPlaceInSarajevoActual: Address = osmManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInSarajevoExpected: Address =
  //      Address(
  //        Some("Adema Buće"),
  //        None,
  //        None,
  //        Some("Entity Federation of Bosnia and Herzegovina"),
  //        Some("Bosnia and Herzegovina"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("residential"),
  //        Some(39.57283980671295)
  //      )
  //
  //    randomPlaceInSarajevoActual should be(randomPlaceInSarajevoExpected)
  //  }


  //  "Reverse geocoding on Bulgaria" should "work" in {
  //    val lat = 42.720754
  //    val lon = 23.273139
  //    val randomPlaceInSarajevoActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInSarajevoExpected: Address =
  //      Address(
  //        Some("бул. Сливница"),
  //        Some("Sofia City"),
  //        None,
  //        None,
  //        Some("Bulgaria"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Primary"),
  //        Some(34.05613156394398)
  //      )
  //
  //    randomPlaceInSarajevoActual should be (randomPlaceInSarajevoExpected)
  //  }
  //
  //  "Reverse geocoding on Cyprus" should "work" in {
  //    val lat = 42.720754
  //    val lon = 23.273139
  //    val randomPlaceInSarajevoActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInSarajevoExpected: Address =
  //      Address(
  //        Some("Digeni Akrita"),
  //        Some("Nicosia"),
  //        None,
  //        None,
  //        Some("Cyprus"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Primary"),
  //        Some(10.075845379599382)
  //      )
  //
  //    randomPlaceInSarajevoActual should be (randomPlaceInSarajevoExpected)
  //  }
  //
  //  "Reverse geocoding on Czech Republic" should "work" in {
  //    val lat = 50.068558
  //    val lon = 14.466333
  //    val randomPlaceInPragueActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInPragueExpected: Address =
  //      Address(
  //        Some("Vršovická"),
  //        Some("Prague"),
  //        None,
  //        None,
  //        Some("Czechia"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(8.918821464337015)
  //      )
  //
  //    randomPlaceInPragueActual should be (randomPlaceInPragueExpected)
  //  }
  //
  //  "Reverse geocoding on Denmark" should "work" in {
  //    val lat = 50.068558
  //    val lon = 14.466333
  //    val randomPlaceInCopenaghenActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInCopenaghenExpected: Address =
  //      Address(
  //        Some("Dag Hammarskjölds Allé"),
  //        Some("Copenhagen Municipality"),
  //        None,
  //        Some("Capital Region of Denmark"),
  //        Some("Denmark"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Tertiary"),
  //        Some(29.492012321257846)
  //      )
  //
  //    randomPlaceInCopenaghenActual should be (randomPlaceInCopenaghenExpected)
  //  }
  //
  //  "Reverse geocoding on Finland" should "work" in {
  //    val lat = 65.009899
  //    val lon = 25.473186
  //
  //    val randomPlaceInCopenaghenActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInCopenaghenExpected: Address =
  //      Address(
  //        Some("Saaristonkatu"),
  //        Some("Oulu"),
  //        Some("Northern Ostrobothnia"),
  //        Some("Northern Finland"),
  //        Some("Finland"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(3.295226571041445)
  //      )
  //
  //    randomPlaceInCopenaghenActual should be (randomPlaceInCopenaghenExpected)
  //  }
  //
  //  "Reverse geocoding on France" should "work" in {
  //    val lat = 47.227236
  //    val lon = -1.542351
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Rue Général Buat"),
  //        Some("Nantes"),
  //        Some("Loire-Atlantique"),
  //        Some("Pays de la Loire"),
  //        Some("France"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(10.739844632084663)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on UK" should "work" in {
  //    val lat = 53.953660
  //    val lon = -1.086269
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Price's Lane"),
  //        Some("Fulford"),
  //        None,
  //        Some("England"),
  //        Some("United Kingdom"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Primary"),
  //        Some(7.1154712254338)
  //      )
  //
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Hungary" should "work" in {
  //    val lat = 47.521604
  //    val lon = 19.104210
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Nagy Lajos király útja"),
  //        Some("Budapest"),
  //        Some("Budapest"),
  //        Some("Central Hungary"),
  //        Some("Hungary"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(20.539271078231685)
  //      )
  //
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Iceland" should "work" in {
  //    val lat = 64.141084
  //    val lon = -21.910899
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Stórholt"),
  //        None,
  //        Some("Reykjavik"),
  //        Some("Capital Region"),
  //        Some("Iceland"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("residential"),
  //        Some(7.42446591656155)
  //      )
  //
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Ireland" should "work" in {
  //    val lat = 53.336276
  //    val lon = -6.273190
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Clanbrassil Street Lower"),
  //        Some("Merchants Quay D ED"),
  //        Some("County Dublin"),
  //        None,
  //        Some("Ireland"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(5.46379611646822)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Kosovo" should "work" in {
  //    val lat = 42.657664
  //    val lon = 21.152872
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Ukshin Hoti"),
  //        Some("Pristina"),
  //        None,
  //        None,
  //        Some("Kosovo"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Tertiary"),
  //        Some(45.100848255476635)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Lichtenstein" should "work" in {
  //    val lat = 47.141686
  //    val lon = 9.521952
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Altenbach"),
  //        Some("Vaduz"),
  //        None,
  //        None,
  //        Some("Liechtenstein"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("residential"),
  //        Some(9.957508778968133)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Luxembourg" should "work" in {
  //    val lat = 49.613429
  //    val lon = 6.127984
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Avenue de la Porte-Neuve"),
  //        Some("Luxembourg"),
  //        Some("Canton Luxembourg"),
  //        None,
  //        Some("Luxembourg"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Tertiary"),
  //        Some(14.532325503150412)
  //      )
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Macedonia" should "work" in {
  //    val lat = 41.998822
  //    val lon = 21.422276
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Булевар Партизански Одреди"),
  //        Some("Skopje"),
  //        None,
  //        Some("Skopje Region"),
  //        Some("Macedonia"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Primary"),
  //        Some(47.69677372342872)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Malta" should "work" in {
  //    val lat = 35.899512
  //    val lon = 14.513850
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Triq l-Arcisqof"),
  //        Some("Valletta"),
  //        None,
  //        Some("Malta"),
  //        Some("Malta"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("residential"),
  //        Some(2.2912572461583207)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Moldova" should "work" in {
  //    val lat = 47.036506
  //    val lon = 28.810874
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some(""),
  //        Some("Chisinau"),
  //        Some("Municipiul Chișinău"),
  //        None,
  //        Some("Moldova"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Footway"),
  //        Some(46.85368689533405)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Monaco" should "work" in {
  //    val lat = 43.739938
  //    val lon = 7.426881
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Allées des Boulingrins"),
  //        Some("Monaco"),
  //        None,
  //        None,
  //        Some("Monaco"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("residential"),
  //        Some(26.93145840936392)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Montenegro" should "work" in {
  //    val lat = 42.449766
  //    val lon = 19.242279
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Dalmatinska"),
  //        Some("Podgorica"),
  //        Some("Podgorica Municipality"),
  //        None,
  //        Some("Montenegro"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(7.344042941564722)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Netherlands" should "work" in {
  //    val lat = 52.369365
  //    val lon = 4.909562
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Anne Frankstraat"),
  //        Some("Amsterdam"),
  //        None,
  //        Some("North Holland"),
  //        Some("Netherlands"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Unclassified"),
  //        Some(8.239906378981507)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Norway" should "work" in {
  //    val lat = 59.909123
  //    val lon = 10.750184
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Langkaigata"),
  //        Some("Oslo"),
  //        None,
  //        None,
  //        Some("Norway"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Unclassified"),
  //        Some(8.68054267952573)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Latvia" should "work" in {
  //    val lat = 56.960431
  //    val lon = 24.120146
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some(""),
  //        None,
  //        Some("Riga"),
  //        Some("Vidzeme"),
  //        Some("Latvia"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Footway"),
  //        Some(18.481130652035016)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Portugal" should "work" in {
  //    val lat = 38.717484
  //    val lon = -9.135806
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Rua da Palma"),
  //        Some("Santa Maria Maior"),
  //        Some("Lisbon"),
  //        Some("Área Metropolitana de Lisboa"),
  //        Some("Portugal"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Primary"),
  //        Some(42.773678496207665)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Romania" should "work" in {
  //    val lat = 44.427569
  //    val lon = 26.033978
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some(""),
  //        None,
  //        None,
  //        Some("Bucharest"),
  //        Some("Romania"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Unclassified"),
  //        Some(4.155840721671128)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Serbia" should "work" in {
  //    val lat = 44.804194
  //    val lon = 20.465830
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Краља Милана"),
  //        Some("Vracar Municipality"),
  //        Some("Belgrade"),
  //        Some("Central Serbia"),
  //        Some("Serbia"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(40.88218640466591)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Slovacchia" should "work" in {
  //    val lat = 48.146618
  //    val lon = 17.122992
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Mlynské nivy"),
  //        Some("District of Bratislava I"),
  //        Some("Bratislava"),
  //        Some("Region of Bratislava"),
  //        Some("Slovakia"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Tertiary"),
  //        Some(5.674711200065942)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Slovenia" should "work" in {
  //    val lat = 46.056785
  //    val lon = 14.501295
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Celovška cesta"),
  //        Some("Ljubljana"),
  //        None,
  //        Some("Osrednjeslovenska"),
  //        Some("Slovenia"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Primary"),
  //        Some(3.6013247504477994)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on switzerland" should "work" in {
  //    val lat = 46.506938
  //    val lon = 6.626941
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Avenue d'Ouchy"),
  //        Some("Lausanne"),
  //        Some("District de Lausanne"),
  //        Some("Vaud"),
  //        Some("Switzerland"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(2.5432399416496927)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on lithuania" should "work" in {
  //    val lat = 54.691931
  //    val lon = 25.280059
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Kalvarijų g."),
  //        None,
  //        None,
  //        Some("Vilnius city municipality"),
  //        Some("Lithuania"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Primary"),
  //        Some(1.3140032180837364)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on sweden" should "work" in {
  //    val lat = 57.708374
  //    val lon = 11.972684
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Burggrevegatan"),
  //        Some("Göteborgs Stad"),
  //        None,
  //        None,
  //        Some("Sweden"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(8.528031530894436)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on ukraine" should "work" in {
  //    val lat = 50.394079
  //    val lon = 30.479168
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Охтирський провулок"),
  //        Some("Novosilky"),
  //        Some("Kyievo-Sviatoshynskyi district"),
  //        Some("Kyiv Oblast"),
  //        Some("Ukraine"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("Secondary"),
  //        Some(18.635041492920582)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Poland" should "work" in {
  //    val lat = 50.072539
  //    val lon = 19.961045
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Orląt Lwowskich"),
  //        None,
  //        None,
  //        None,
  //        Some("Poland"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("residential"),
  //        Some(30.077212204176767)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }
  //
  //  "Reverse geocoding on Spain" should "work" in {
  //    val lat = 40.443007
  //    val lon = -3.678156
  //
  //    val randomPlaceInFranceActual: Address = ReverseGeocodingManager.reverseGeocode(lat, lon)
  //
  //    val randomPlaceInFranceExpected: Address =
  //      Address(
  //        Some("Calle de Josep Plá"),
  //        Some("Madrid"),
  //        Some("Community of Madrid"),
  //        Some("Community of Madrid"),
  //        Some("Spain"),
  //        None,
  //        None,
  //        None,
  //        None,
  //        Some("residential"),
  //        Some(39.22776000155658)
  //      )
  //
  //    randomPlaceInFranceActual should be (randomPlaceInFranceExpected)
  //  }

}

