package ch.uzh.ifi.hase.soprafs26.constants;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;


import ch.uzh.ifi.hase.soprafs26.constant.GameRegions;
import ch.uzh.ifi.hase.soprafs26.constant.SearchRegion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;



public class GameRegionsTest {

    @Test
    void testGetRegionsWithValidAlps() {
        List<SearchRegion> regions = GameRegions.getRegions("Alps");
        
        assertNotNull(regions);
        assertEquals(2, regions.size());
    }

    @Test
    void testGetRegionsWithValidNewZealandAlps() {
        List<SearchRegion> regions = GameRegions.getRegions("NewZealandAlps");
        
        assertNotNull(regions);
        assertEquals(1, regions.size());
    }

    @Test
    void testGetRegionsWithValidHimalaya() {
        List<SearchRegion> regions = GameRegions.getRegions("Himalaya");
        
        assertNotNull(regions);
        assertEquals(1, regions.size());
    }

    @Test
    void testGetRegionsWithValidJapaneseAlps() {
        List<SearchRegion> regions = GameRegions.getRegions("JapaneseAlps");
        
        assertNotNull(regions);
        assertEquals(1, regions.size());
    }

    @Test
    void testGetRegionsWithValidAndes() {
        List<SearchRegion> regions = GameRegions.getRegions("Andes");
        
        assertNotNull(regions);
        assertEquals(1, regions.size());
    }

    @Test
    void testGetRegionsInvalid() {
        List<SearchRegion> regions = GameRegions.getRegions("SomeMountains");
        
        assertNotNull(regions);
        assertEquals(0, regions.size());
    }
    @Test
    public void testGetRegionMap() {
        Map<String, List<SearchRegion>> regionMap = GameRegions.getRegionMap();

        assertNotNull(regionMap);
        assertTrue(regionMap.containsKey("Alps"));
        assertTrue(regionMap.containsKey("NewZealandAlps"));
        assertTrue(regionMap.containsKey("Himalaya"));
        assertTrue(regionMap.containsKey("JapaneseAlps"));
        assertTrue(regionMap.containsKey("Andes"));
        assertEquals(5, regionMap.size());
    }


    @Test
    public void testGetAllRegionsList() {
        List<SearchRegion> allRegions = GameRegions.getAllRegionsList();
        assertNotNull(allRegions);
        assertEquals(6, allRegions.size());
    }   
}
