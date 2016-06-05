package rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class WebErrorManagerTest {

    @Test
    public void testShowFieldsNotPresent() throws JSONException {
        final JSONObject json = new JSONObject().put("field_a", "");
        
        final JSONArray actualErrors = WebErrorManager.showFieldsNotPresent(json, "field_a", "field_b");
        final JSONArray predictedErrors = new JSONArray().put( new JSONObject().put("field_b", "Field \"field_b\" not present!"));

        assertNotNull(actualErrors);
        assertEquals(predictedErrors.toString(), actualErrors.toString());
    }
}