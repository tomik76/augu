import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

  private ArFragment arFragment;
//ddddddd
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
    arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);
  }

  private void onSceneUpdate(FrameTime frameTime) {
    Frame frame = arFragment.getArSceneView().getArFrame();
    if (frame == null) {
      return;
    }

    if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
      return;
    }

    for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
      if (plane.getTrackingState() == TrackingState.TRACKING) {
        placeObject(arFragment, plane.createAnchor(plane.getCenterPose()));
      }
    }
  }

  private void placeObject(ArFragment fragment, Anchor anchor) {
    ModelRenderable.builder()
        .setSource(this, R.raw.andy)
        .build()
        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
        .exceptionally(throwable -> {
          return null;
        });
  }

  private void addNodeToScene(ArFragment fragment, Anchor anchor, ModelRenderable renderable) {
    AnchorNode anchorNode = new AnchorNode(anchor);
    TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
    node.setRenderable(renderable);
    node.setParent(anchorNode);
    fragment.getArSceneView().getScene().addChild(anchorNode);
    node.select();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (arFragment.getArSceneView().getSession() == null) {
      try {
        Session session = new Session(this);
        arFragment.getArSceneView().setupSession(session);
      } catch
