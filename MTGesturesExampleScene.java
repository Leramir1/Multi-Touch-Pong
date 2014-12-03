package basic.mtGestures;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.font.IFont;
import org.mt4j.components.visibleComponents.shapes.MTRectangle.PositionAnchor;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.ToolsMath;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;
import advanced.physics.physicsShapes.IPhysicsComponent;
import advanced.physics.physicsShapes.PhysicsCircle;
import advanced.physics.physicsShapes.PhysicsRectangle;
import advanced.physics.util.PhysicsHelper;
import advanced.physics.util.UpdatePhysicsAction;

public class MTGesturesExampleScene extends AbstractScene {
	private MTApplication app;
	private World world;
	private int scale = 20;
	private HockeyBall ball;
	
	public MTGesturesExampleScene(MTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.app = mtApplication;
		
		this.registerGlobalInputProcessor(new CursorTracer(app, this));

		float verticalPad = 53;
		float horizontalPad = 500;
		
		MTColor white = new MTColor(255,255,255);
		final MTColor textAreaColor = new MTColor(50,50,50,255);
		
		IFont font = FontManager.getInstance().createFont(app, "arial.ttf", 35, white, white);

		Vec2 gravity = new Vec2(0,0);
        AABB worldAABB = new AABB();
        worldAABB.upperBound.set(100, 100);
        worldAABB.lowerBound.set(-100, -100);
        boolean sleep = true;
        world = new World(worldAABB, gravity, sleep);
          
        MTComponent physicsContainer = new MTComponent(app);
        physicsContainer.scale(20, 20, 1, Vector3D.ZERO_VECTOR);   
        
        this.getCanvas().addChild(physicsContainer);
        this.registerPreDrawAction(new UpdatePhysicsAction(world, 1.0f/30.0f, 10, 20));
		
		//Add Pong bars
        PhysicsRectangle barTop = new PhysicsRectangle(new Vector3D(app.width/2 , 25), 300, 40, app, world, 0,0,0, 20);
        physicsContainer.addChild(barTop);
        PhysicsHelper.addDragJoint(world, barTop, barTop.getBody().isDynamic(), 20);
        
        PhysicsRectangle barBottom = new PhysicsRectangle(new Vector3D(app.width/2 , app.height-25), 300, 40, app, world, 0,0,0, 20);
        physicsContainer.addChild(barBottom);
        PhysicsHelper.addDragJoint(world, barBottom, barBottom.getBody().isDynamic(), 20);
        
        
        //boundaries        
        this.createScreenBorders(physicsContainer);
        
        //create pong ball
		ball = new HockeyBall(app, new Vector3D(app.width/2f, app.height/2f), 38, world, 0.5f, 0, 1, 20);
		ball.setFillColor(new MTColor(255,255,255,255));
		ball.setNoStroke(true);
		physicsContainer.addChild(ball);
		ball.getBody().setLinearVelocity(new Vec2(ToolsMath.getRandom(-10, 10),ToolsMath.getRandom(-25, 25)));
		
		//Double Tap gesture
		final MTTextArea doubleTap = new MTTextArea(app, font);
		doubleTap.setFillColor(textAreaColor);
		doubleTap.setStrokeColor(textAreaColor);
		doubleTap.setText("Reset ball");
		this.clearAllGestures(doubleTap);
		doubleTap.registerInputProcessor(new TapProcessor(app, 25, true, 350));
		doubleTap.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapEvent te = (TapEvent)ge;
				if (te.isTapped()){
					ball.getBody().setXForm(new Vec2(app.width/2f/scale, app.height/2f/scale), ball.getBody().getAngle());
					ball.getBody().setLinearVelocity(new Vec2(ToolsMath.getRandom(-10, 10),ToolsMath.getRandom(-25, 25)));
				}
				return false;
			}
		});
		this.getCanvas().addChild(doubleTap);
		doubleTap.setPositionGlobal(new Vector3D(app.width/2, app.height/2));
		
	}
	
	private void clearAllGestures(MTComponent comp){
		comp.unregisterAllInputProcessors();
	}
	
	@Override
	public void init() {

	}

	@Override
	public void shutDown() {

	}
	
	private class HockeyBall extends PhysicsCircle{
		public HockeyBall(PApplet applet, Vector3D centerPoint, float radius,
				World world, float density, float friction, float restitution, float worldScale) {
			super(applet, centerPoint, radius, world, density, friction, restitution, worldScale);
		} 
		
		@Override
		protected void circleDefB4CreationCallback(CircleDef def) {
			super.circleDefB4CreationCallback(def);
			def.radius = def.radius -5/20;
		}
		@Override
		protected void bodyDefB4CreationCallback(BodyDef def) {
			super.bodyDefB4CreationCallback(def);
			def.isBullet = true;
			
			def.fixedRotation = true;
		}
	}
	
	private void createScreenBorders(MTComponent parent){
		//Left border 
		float borderWidth = 50f;
		float borderHeight = app.height;
		Vector3D pos = new Vector3D(-(borderWidth/2f) , app.height/2f);
		PhysicsRectangle borderLeft = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderLeft.setName("borderLeft");
		parent.addChild(borderLeft);
		
		//Right border
		pos = new Vector3D(app.width + (borderWidth/2), app.height/2);
		PhysicsRectangle borderRight = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderRight.setName("borderRight");
		parent.addChild(borderRight);
	}

}
