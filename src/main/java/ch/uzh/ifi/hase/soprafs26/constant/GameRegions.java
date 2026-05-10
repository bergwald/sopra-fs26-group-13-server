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
                                        new SearchRegion("AlpsBoxNorth", 5.943604, 46.057976, 16.677246, 48.180734),
                                        new SearchRegion("AlpsBoxSouth", 5.185547, 43.620213, 7.558594, 46.111319))),
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
