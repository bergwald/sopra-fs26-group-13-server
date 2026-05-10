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
                                        List.of(new SearchRegion("NewZealandAlps", 165.366211, -47.676238, 177.429199,
                                                        -38.872305))),
                        Map.entry("Himalaya",
                                        List.of(new SearchRegion("HimalayaCentral", 66.401367, 26.155291, 103.754883,
                                                        44.495013))),
                        Map.entry("JapaneseAlps",
                                        List.of(new SearchRegion("JapaneseAlps", 136.582031, 34.385028, 142.998047,
                                                        41.214540))),
                        Map.entry("Andes", List
                                        .of(new SearchRegion("Andes", -80.507813, -55.749307, -66.093750, 12.726004))));

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
