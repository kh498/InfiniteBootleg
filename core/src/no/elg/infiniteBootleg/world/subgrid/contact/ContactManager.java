package no.elg.infiniteBootleg.world.subgrid.contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import no.elg.infiniteBootleg.world.World;
import no.elg.infiniteBootleg.world.render.WorldRender;
import no.elg.infiniteBootleg.world.subgrid.Entity;
import org.jetbrains.annotations.NotNull;

public class ContactManager implements ContactListener {

    private final World world;

    public ContactManager(@NotNull World world) {
        this.world = world;
    }

    @Override
    public void beginContact(Contact contact) {
        synchronized (WorldRender.BOX2D_LOCK) {
            for (Entity entity : world.getEntities()) {
                if (contact.getFixtureB().getBody() == entity.getBody()) {
                    ((ContactHandler) entity).contact(ContactType.BEGIN_CONTACT, contact);
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        synchronized (WorldRender.BOX2D_LOCK) {
            for (Entity entity : world.getEntities()) {
                if (contact.getFixtureB().getBody() == entity.getBody()) {
                    ((ContactHandler) entity).contact(ContactType.END_CONTACT, contact);
                }
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) { }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) { }
}
