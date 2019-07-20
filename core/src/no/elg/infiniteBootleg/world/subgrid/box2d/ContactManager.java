package no.elg.infiniteBootleg.world.subgrid.box2d;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import no.elg.infiniteBootleg.world.World;
import no.elg.infiniteBootleg.world.subgrid.Entity;
import org.jetbrains.annotations.NotNull;

public class ContactManager implements ContactListener {

    private World world;

    public ContactManager(@NotNull World world) {
        this.world = world;
    }

    @Override
    public void beginContact(Contact contact) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof ContactHandler && contact.getFixtureB().getBody() == entity.getBody()) {
                ((ContactHandler) entity).contact(ContactType.BEGIN_CONTACT, contact);
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof ContactHandler && contact.getFixtureB().getBody() == entity.getBody()) {
                ((ContactHandler) entity).contact(ContactType.END_CONTACT, contact);
            }

        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) { }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) { }
}
