package uk.co.mattburns.checkmend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import uk.co.mattburns.checkmend.Property.Category;

public class CheckmendTest {

    private long partnerid;
    private String secretKey;

    @Before
    public void before() {
        Properties props = new Properties();

        try {
            // load a properties file
            props.load(getClass().getResourceAsStream(
                    "test-settings.properties"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        partnerid = Long.parseLong((String) props.get("PARTNER_ID"));
        secretKey = (String) props.get("SECRET_KEY");
    }

    @Test
    public void can_parse_error_json() {
        // @formatter:off
        CheckmendError error = Checkmend.jsonToCheckMENDError("" +
       		"{" +
       		"    \"errors\":" +
       		"    [" +
       		"        {" +
       		"            \"id\": 1," +
       		"            \"message\": \"Human readable message\"" +
       		"        }," +
       		"        {" +
       		"            \"id\": 3," +
       		"            \"message\": \"Another human readable message\"" +
       		"        }" +
       		"    ]" +
       		"}");
        // @formatter:on

        assertEquals(1, error.getErrors().get(0).getId());
        assertEquals("Human readable message", error.getErrors().get(0)
                .getMessage());

        assertEquals(3, error.getErrors().get(1).getId());
        assertEquals("Another human readable message", error.getErrors().get(1)
                .getMessage());
    }

    @Test
    public void can_generate_signature_hash() {
        // example taken from http://gapi.checkmend.com/docs/api-usage.php
        String hash = Checkmend.generateSignatureHash(1, "234623ger787qws3423",
                "{\"category\": 8}");

        assertEquals(
                "MTpmZjYwMjM2MDI2OTU1ODA3NDNmODRmZWI4ZjI3NTBkZTg5ZTBkM2Q2",
                hash);
    }

    @Test
    public void can_register_person() {
        Person bob = new Person.PersonBuilder("ref123").withFamilyname("smith")
                .withOthernames("bob").build();

        Checkmend checkmend = new Checkmend(partnerid, secretKey, System.out);

        long personid = checkmend.registerPerson(bob);
        checkmend.removePerson(personid);
    }

    @Test
    public void removing_invalid_person_throws_exception() {
        Checkmend checkmend = new Checkmend(partnerid, secretKey, System.out);
        try {
            checkmend.removePerson(1);
            fail("Should have thrown");
        } catch (CheckmendError e) {
            assertEquals(1, e.getErrors().size());
            assertEquals(801, e.getErrors().get(0).getId());
            assertEquals("The Person ID supplied is invalid.", e.getErrors()
                    .get(0).getMessage());
        }
    }

    @Test
    public void can_register_property() {
        Person bob = new Person.PersonBuilder("ref123").withFamilyname("smith")
                .withOthernames("bob").build();

        Checkmend checkmend = new Checkmend(partnerid, secretKey, System.out);

        long personid = checkmend.registerPerson(bob);

        try {
            Property property = new Property.PropertyBuilder(personid,
                    Category.Camera, "Canon", "123").withModel("7D")
                    .withDescription("My camera").build();

            long propertyid = checkmend.registerProperty(property);

            checkmend.removeProperty(propertyid);
        } finally {
            checkmend.removePerson(personid);
        }
    }
}
