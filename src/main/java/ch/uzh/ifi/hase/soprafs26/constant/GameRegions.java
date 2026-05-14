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
                                        new SearchRegion("AlpsZermatt", 6.855469,45.863505,8.000793,46.081050),
                                        new SearchRegion("AlpsCourmayeur", 6.803284,45.493831,7.110901,45.861311),
                                        new SearchRegion("AlpsBessans", 6.470947,45.167528,7.461090,45.455854),
                                        new SearchRegion("AlpsArvieux", 6.580811,44.386681,7.152100,45.176255),
                                        new SearchRegion("AlpsOberwald", .520142,46.451095,8.517151,46.576797),
                                        new SearchRegion("AlpsStMoritz", 9.096680,46.344060,10.328522,46.623004),
                                        new SearchRegion("AlpsSamaun", 9.729767,46.741731,11.336517,46.921188),
                                        new SearchRegion("AlpsArlberg", 10.065536,46.929633,10.464478,47.265722),
                                        new SearchRegion("AlpsStubai", 11.063232,46.929164,11.412735,47.192974),
                                        new SearchRegion("AlpsTauern", 12.547073,47.042986,13.046951,47.191248))),
                        Map.entry("NewZealandAlps",
                                        List.of(new SearchRegion("NewZealandAlpsQueensTown", 166.931763, -46.000200,
                                                        169.650879, -44.225486),
                                                        new SearchRegion("NewZealandAlpsUp", 170.837402, -43.268105,
                                                                        172.880859, -42.355545))),
                        Map.entry("Himalaya",
                                        List.of(new SearchRegion("HimalayaCentralRight", 77.783203, 26.800268,
                                                        103.842774, 33.615246),
                                                        new SearchRegion("HimalayaCentralRight", 67.620850, 33.356387,
                                                                        78.629150, 39.273202))),
                        Map.entry("JapaneseAlps",
                                        List.of(new SearchRegion("JapaneseAlps", 137.362061, 35.304546, 139.394531,
                                                        36.923664))),
                        Map.entry("Andes", List
                                        .of(new SearchRegion("Andes", -71.674805, -37.510203, -65.346680,
                                                        -15.103670))));

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
