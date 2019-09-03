package com.jkspad.tutorial;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * @author John Knight
 * Copyright http://www.jkspad.com
 *
 */
public class AspectRatio extends ApplicationAdapter  implements InputProcessor{

	private Mesh quadMesh;
	private Mesh quadMesh2;
	private ShaderProgram shader;
	private OrthographicCamera camera;
	private OrthographicCamera camera2;
	private OrthographicCamera camera3;

	private SpriteBatch spriteBatch;
	private BitmapFont font;

	private static final int TEXT_X = 10;
	private static final int TEXT_Y = 20;

	private enum State{
		Camera1,
		Camera2,
		Camera3,
		End;

		public State next() {
			State next = values()[ordinal() + 1];
			if(next == End){
				return values()[0];
			}
			return next;
		}
	}

	private State state = State.Camera1;

	private final String VERTEX_SHADER =
			"attribute vec4 a_position;\n"
					+ "attribute vec4 a_color;\n"
					+ "uniform mat4 u_proj;\n"
					+ "varying vec4 v_color;\n"
					+ "void main() {\n"
					+ " gl_Position = u_proj * a_position;\n"
					+ " v_color = a_color;\n" +
					"}";

	private final String FRAGMENT_SHADER =
			"varying vec4 v_color;\n"
					+ "void main() {\n"
					+ " gl_FragColor = v_color;\n"
					+ "}";


	protected void createMeshShader() {
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
		String log = shader.getLog();
		if (!shader.isCompiled()){
			throw new GdxRuntimeException(log);
		}
		if (log!=null && log.length()!=0){
			Gdx.app.log("shader log", log);
		}
	}


	private void createQuadMesh(){
		if (quadMesh == null) {
			quadMesh = new Mesh(true, 4, 0,
					new VertexAttribute(Usage.Position, 3, "a_position"),
					new VertexAttribute(Usage.ColorPacked, 4, "a_color"));

			// Top red to bottom white
			quadMesh.setVertices(new float[] {
					-0.5f, -0.5f, 0, Color.toFloatBits(255, 255, 255, 255), // white bottom left
					0.5f, -0.5f, 0, Color.toFloatBits(255, 255, 255, 255), 	// white bottom right
					-0.5f, 0.5f, 0,  Color.toFloatBits(255, 0, 0, 255),		// red red top left
					0.5f, 0.5f, 0,  Color.toFloatBits(255, 0, 0, 255) });	// red top right
		}

		if (quadMesh2 == null) {
			quadMesh2 = new Mesh(true, 4, 0,
					new VertexAttribute(Usage.Position, 3, "a_position"),
					new VertexAttribute(Usage.ColorPacked, 4, "a_color"));

			// Top green to bottom white
			quadMesh2.setVertices(new float[] {
					-120f, -120f, 0, Color.toFloatBits(255, 255, 255, 255), // white bottom left
					120f, -120f, 0, Color.toFloatBits(255, 255, 255, 255), 	// white bottom right
					-120f, 120f, 0,  Color.toFloatBits(0, 255, 0, 255),		// green top left
					120f, 120f, 0,  Color.toFloatBits(0, 255, 0, 255) });	// green top right
		}

	}

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		createQuadMesh();
		createMeshShader();
		Gdx.input.setInputProcessor(this);
	}

	private void showMessage(){
		spriteBatch.begin();
		font.draw(spriteBatch, "Hit space and/or resize baby!", TEXT_X, TEXT_Y);
		spriteBatch.end();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		camera2.update();
		camera3.update();

		shader.begin();
		switch(state){
			case Camera1:
				shader.setUniformMatrix("u_proj", camera.projection);
				quadMesh.render(shader, GL20.GL_TRIANGLE_STRIP, 0, 4);
				break;
			case Camera2:
				shader.setUniformMatrix("u_proj", camera2.projection);
				quadMesh.render(shader, GL20.GL_TRIANGLE_STRIP, 0, 4);
				break;
			case Camera3:
				shader.setUniformMatrix("u_proj", camera3.projection);
				quadMesh2.render(shader, GL20.GL_TRIANGLE_STRIP, 0, 4);
				break;
			default:
				break;
		}
		shader.end();
		showMessage();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);

		float aspectRatio = 1;
		if( width > height ){
			aspectRatio = (float) width / (float) height;
			camera = new OrthographicCamera(2, 2);
			camera2 = new OrthographicCamera(2 * aspectRatio, 2);
		}else{
			camera = new OrthographicCamera(2, 2 * aspectRatio);
			camera2 = new OrthographicCamera(2, 2 * aspectRatio);
		}

		// Note that we are explicitly setting the camera to the width and height
		// so there is no need to use the aspect ratio factor here
		camera3 = new OrthographicCamera(width, height);
	}

	@Override
	public void dispose() {
		super.dispose();
		shader.dispose();
		quadMesh.dispose();
	}


	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Keys.SPACE){
			state = state.next();
			return true;
		}
		return false;
	}


	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
