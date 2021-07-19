### Changelog

All notable changes to this project will be documented in this file. Dates are displayed in UTC.

#### [v1.3.1](https://github.com/agile-lab-dev/GIS.Lib/compare/v1.3.0...v1.3.1)

- [#95] - NPE when load boundary with null geometry [`c51c281`](https://github.com/agile-lab-dev/GIS.Lib/commit/c51c281d3f1ab0dc4d8a089f950b90cf46715cec)
- Bump version to v1.3.1 [`4271836`](https://github.com/agile-lab-dev/GIS.Lib/commit/42718362d3dc66ab358748b2882de7e5edff2bdf)

#### [v1.3.0](https://github.com/agile-lab-dev/GIS.Lib/compare/v1.2.2...v1.3.0)

> 9 July 2021

- [#72] Handle postalcodes as polygons [`#73`](https://github.com/agile-lab-dev/GIS.Lib/pull/73)
- Add CI configuration [`#57`](https://github.com/agile-lab-dev/GIS.Lib/pull/57)
- [#85] Add scaladoc and a bit of readme documentation [`#86`](https://github.com/agile-lab-dev/GIS.Lib/pull/86)
- [#81] Support specifying reverse geocoding indices to query [`#82`](https://github.com/agile-lab-dev/GIS.Lib/pull/82)
- Bump Scala to 2.11.12 [`#77`](https://github.com/agile-lab-dev/GIS.Lib/pull/77)
- Exclude Jackson core and databind dependencies [`#75`](https://github.com/agile-lab-dev/GIS.Lib/pull/75)
- [#72] Handle postalcodes as polygons (#73) [`#72`](https://github.com/agile-lab-dev/GIS.Lib/issues/72)
- Add CI configuration (#57) [`#56`](https://github.com/agile-lab-dev/GIS.Lib/issues/56)
- [#85] Add scaladoc and a bit of readme documentation (#86) [`#85`](https://github.com/agile-lab-dev/GIS.Lib/issues/85)
- [#64] Fix bad log message [`#64`](https://github.com/agile-lab-dev/GIS.Lib/issues/64)
- [#81] Support specifying reverse geocoding indices to query (#82) [`#81`](https://github.com/agile-lab-dev/GIS.Lib/issues/81)
- Bump Scala to 2.11.12 (#77) [`#76`](https://github.com/agile-lab-dev/GIS.Lib/issues/76)
- Exclude Jackson core and databind dependencies (#75) [`#74`](https://github.com/agile-lab-dev/GIS.Lib/issues/74)

#### [v1.2.2](https://github.com/agile-lab-dev/GIS.Lib/compare/v1.2.1...v1.2.2)

> 25 May 2021

- Refactor CarFlagEncoderEnrich bits shifts [`a595298`](https://github.com/agile-lab-dev/GIS.Lib/commit/a5952985f9d550e12a7c35975d70f6723861b7bd)
- bump to version 1.2.2 [`ee690e0`](https://github.com/agile-lab-dev/GIS.Lib/commit/ee690e06ac8eb801873def84f23c5175b7c1d119)

#### [v1.2.1](https://github.com/agile-lab-dev/GIS.Lib/compare/v1.2.0...v1.2.1)

> 25 May 2021

- [#78] Revert #69 [`e466809`](https://github.com/agile-lab-dev/GIS.Lib/commit/e4668099f9b64cb8d81ed5d209f0d56cd5fb2871)
- bump version to 1.2.1 [`451b1af`](https://github.com/agile-lab-dev/GIS.Lib/commit/451b1af9df9adf4056f54761020751d97cf79ba8)

#### [v1.2.0](https://github.com/agile-lab-dev/GIS.Lib/compare/v1.1.2...v1.2.0)

> 14 May 2021

- Add more highways to the car flag encoder's highway list [`#69`](https://github.com/agile-lab-dev/GIS.Lib/pull/69)
- Add Scalafmt configuration [`#68`](https://github.com/agile-lab-dev/GIS.Lib/pull/68)
- Add regions index [`#65`](https://github.com/agile-lab-dev/GIS.Lib/pull/65)
- Route matching in short trips with small movements returns valid route [`#67`](https://github.com/agile-lab-dev/GIS.Lib/pull/67)
- Serialize OSM index for faster app startup [`#63`](https://github.com/agile-lab-dev/GIS.Lib/pull/63)
- Optimize house numbers loading [`#61`](https://github.com/agile-lab-dev/GIS.Lib/pull/61)
- Update OSM maps and take administrative level/value from config [`#55`](https://github.com/agile-lab-dev/GIS.Lib/pull/55)
- Improve exception's message on unrecognized administrative level [`#52`](https://github.com/agile-lab-dev/GIS.Lib/pull/52)
- [#70] Settings to deploy on maven [`#70`](https://github.com/agile-lab-dev/GIS.Lib/issues/70)
- Serialize OSM index for faster app startup (#63) [`#62`](https://github.com/agile-lab-dev/GIS.Lib/issues/62)
- Optimize house numbers loading (#61) [`#60`](https://github.com/agile-lab-dev/GIS.Lib/issues/60)
- [#63] Use logger to log ConverterFromOSMToGraphHopperMap arguments [`#58`](https://github.com/agile-lab-dev/GIS.Lib/issues/58)
- Update OSM maps and take administrative level/value from config (#55) [`#53`](https://github.com/agile-lab-dev/GIS.Lib/issues/53) [`#54`](https://github.com/agile-lab-dev/GIS.Lib/issues/54)
- [#50] Enrich IndexManager with house numbers [`#50`](https://github.com/agile-lab-dev/GIS.Lib/issues/50)
- [#46] Enrich IndexManager with postalcode [`#46`](https://github.com/agile-lab-dev/GIS.Lib/issues/46)
- [#47] Add id to ReverseGeocodingResponse and GPSPoint [`#47`](https://github.com/agile-lab-dev/GIS.Lib/issues/47)
- [#45] Handle error in MM and RG api [`#32`](https://github.com/agile-lab-dev/GIS.Lib/issues/32)
- [#39] Remove log4j dependency [`#39`](https://github.com/agile-lab-dev/GIS.Lib/issues/39)
-  [#37] fix GraphHopperSpec tests [`#37`](https://github.com/agile-lab-dev/GIS.Lib/issues/37)
- [#30] pass configuration to managers [`#30`](https://github.com/agile-lab-dev/GIS.Lib/issues/30)
- [#28] Publish snapshot on public repository [`#28`](https://github.com/agile-lab-dev/GIS.Lib/issues/28)
- [#25] - Enrich MapMatching response [`#25`](https://github.com/agile-lab-dev/GIS.Lib/issues/25)
- [#24] - Missing information from reverse geocoding [`#24`](https://github.com/agile-lab-dev/GIS.Lib/issues/24)

#### [v1.1.2](https://github.com/agile-lab-dev/GIS.Lib/compare/v1.1.1...v1.1.2)

> 5 February 2021

- [#19] fix configuration file [`8f7eda6`](https://github.com/agile-lab-dev/GIS.Lib/commit/8f7eda693628b0ead0bb056cb6437b1a283990d5)
- bump version to 1.1.2 [`5b9d88f`](https://github.com/agile-lab-dev/GIS.Lib/commit/5b9d88f00fec6e4c32e15f796a6a00453a5fd1d2)

#### [v1.1.1](https://github.com/agile-lab-dev/GIS.Lib/compare/v1.1.0...v1.1.1)

> 1 December 2020

- [#18] added config boundary.administrative section and manage it on OSMAdministrativeBoundariesLoader.scala class [`e6d18c4`](https://github.com/agile-lab-dev/GIS.Lib/commit/e6d18c4e2e5057496f93f1887445a92e4e79df81)
- bum version to v1.1.1 [`c8d8ad7`](https://github.com/agile-lab-dev/GIS.Lib/commit/c8d8ad71eadce12843100036d903286aa8b11d88)
- [#17] updated "geo" repository [`29127f5`](https://github.com/agile-lab-dev/GIS.Lib/commit/29127f5cd50bb0748d80e846c7bb5fd80511808c)

#### [v1.1.0](https://github.com/agile-lab-dev/GIS.Lib/compare/v1.0.0...v1.1.0)

> 1 November 2020

- [#15] Introduce a multipoint category membership info capability [`#15`](https://github.com/agile-lab-dev/GIS.Lib/issues/15)
- [#13] Check if a point is inside a Polygon [`#13`](https://github.com/agile-lab-dev/GIS.Lib/issues/13)

#### v1.0.0

> 11 April 2019

- First version [`eeedab0`](https://github.com/agile-lab-dev/GIS.Lib/commit/eeedab0e48005f918c89dac5f6a23454f7f985db)
- [#1] remove resources not used [`7c25488`](https://github.com/agile-lab-dev/GIS.Lib/commit/7c2548802a15dbcd9d2121bfc8dfb14bd6b7fcd0)
- [#1] remove code not used [`58f056c`](https://github.com/agile-lab-dev/GIS.Lib/commit/58f056c64aa3de9099010eadc566642ea78253c2)
