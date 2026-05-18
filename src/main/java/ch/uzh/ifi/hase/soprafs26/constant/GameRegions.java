package ch.uzh.ifi.hase.soprafs26.constant;

import java.util.List;
import java.util.Map;

/*
Contains a mapping of all game regions. These regions can consist of multiple bounding boxes.
To add new regions with multiple bounding boxes, only extend the entries.
A tool which helps to draw bounding boxes:
https://bboxfinder.com/
*/
public class GameRegions {

        private static final Map<String, List<SearchRegion>> REGIONS = Map.ofEntries(
                        Map.entry("Alps", List.of(
                                        new SearchRegion("AlpsZermatt", 6.855469, 45.863505, 8.000793, 46.081050),
                                        new SearchRegion("AlpsCourmayeur", 6.803284, 45.493831, 7.110901, 45.861311),
                                        new SearchRegion("AlpsBessans", 6.470947, 45.167528, 7.461090, 45.455854),
                                        new SearchRegion("AlpsArvieux", 6.580811, 44.386681, 7.152100, 45.176255),
                                        new SearchRegion("AlpsOberwald", 6.520142, 46.451095, 8.517151, 46.576797),
                                        new SearchRegion("AlpsStMoritz", 9.096680, 46.344060, 10.328522, 46.623004),
                                        new SearchRegion("AlpsSamaun", 9.729767, 46.741731, 11.336517, 46.921188),
                                        new SearchRegion("AlpsArlberg", 10.065536, 46.929633, 10.464478, 47.265722),
                                        new SearchRegion("AlpsStubai", 11.063232, 46.929164, 11.412735, 47.192974),
                                        new SearchRegion("AlpsTauern", 12.547073, 47.042986, 13.046951, 47.191248))),
                        Map.entry("NewZealandAlps",
                                        List.of(new SearchRegion("NewZealandAlpsQueensTown", 166.931763, -46.000200,
                                                        169.650879, -44.225486),
                                                        new SearchRegion("NewZealandAlpsUp", 170.837402, -43.268105,
                                                                        172.880859, -42.355545))),
                        Map.entry("Himalaya",
                                        List.of(new SearchRegion("HimalayaBhutan", 86.113586, 26.927923, 92.370300,
                                                        28.963940),
                                                        new SearchRegion("HimalayaCentralNepal", 83.309326, 27.955591,
                                                                        86.088867, 29.104177),
                                                        new SearchRegion("HimalayaWestNepal", 80.612183, 28.921631,
                                                                        84.155273, 30.600094),
                                                        new SearchRegion("HimalayaNorthEastIndia", 78.211670, 30.656816,
                                                                        81.145020, 33.751748),
                                                        new SearchRegion("HimalayaPakistan", 74.915771, 33.824794,
                                                                        78.239136, 37.579413))),
                        Map.entry("JapaneseAlps",
                                        List.of(new SearchRegion("JapaneseAlps", 137.395020, 35.621582, 137.817993,
                                                        36.752089),
                                                        new SearchRegion("JapaneseAlpsNorth", 138.034973, 35.194743,
                                                                        138.342590, 35.807114),
                                                        new SearchRegion("JapaneseAlpsMtFuji", 138.675613, 35.327731,
                                                                        138.789253, 35.389610))),
                        Map.entry("Andes", List
                                        .of(
                                        new SearchRegion("Andes1", -76.376953,2.376712,-74.882813,7.037022),
                                        new SearchRegion("Andes2", -73.674316,4.720670,-72.180176,8.99361),
                                        new SearchRegion("Andes3", -79.101563,-3.318119,-77.915039,0.724489),
                                        new SearchRegion("Andes4", -79.453125,-7.372414,-78.266602,-3.329805),
                                        new SearchRegion("Andes5", -78.090820,-10.665709,-75.585938,-7.950579),
                                        new SearchRegion("Andes6", -76.113281,-14.541088,-73.850098,-12.188961),
                                        new SearchRegion("Andes7", -74.146729,-16.319964,-69.147949,-13.682901),
                                        new SearchRegion("Andes8", -67.280273,-26.352639,-64.248047,-17.782692),
                                        new SearchRegion("Andes9", -70.817871,-36.156949,-68.466797,-25.563493))));

        public static List<SearchRegion> getRegions(String regionName) {
                return REGIONS.getOrDefault(regionName, List.of());
        }

        public static Map<String, List<SearchRegion>> getRegionMap() {
                return REGIONS;
        }

        public static List<SearchRegion> getAllRegionsList() {
                return REGIONS.values().stream()
                                .flatMap(List::stream)
                                .toList(); // Flattens all regions into a single list
        }
}
