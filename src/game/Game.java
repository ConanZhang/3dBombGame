/**
 * Game.class
 * 
 * Contains:
 * 
 *  Target.class
 *   
 *  Bomb.class
 *      
 *  Camera.class
 *      left()
 *      right()
 *      forward()
 *      back()
 *      sLeft()  -- strafes left
 *      sRight() -- strafes right
 * 
 * This is the class for our 3D game.
 * - Room is built with Quads [located at position -5000]
 * - Targets are cubes that rotate, bombs are spheres
 * - update() function uses 3D Tree to return a certain number of neighbors
 * - display function draws bombs and targets
 * 
 * Special additions:
 * - Booleans used for keypresses so you can move press more than one button at once
 * - setMaterial function to specify ambient/diffuse/color for each material
 * - WASD keys move the camera
 * - Left, Right Arrows make the camera look left and right
 * - Up, Down Arrows increase and decrease the gravity (up to a certain limit)
 * - Space shoots bombs, which shrink down and disappear from the simulation
 **/

package game;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.swing.JApplet;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.fixedfunc.GLLightingFunc;

public class Game extends JApplet implements GLEventListener, KeyListener
{
    /**WINDOW/GLU/CAMERA/BOX SETUP**/
    int winWidth=1000, winHeight=750;
    FPSAnimator animator;
    GLU glu;
    GLUT glut;
    double theta;
    long lastTime;
    Thread updateThread;
    Camera camera;
    double boxWidth, boxHeight, boxLength;//box size
    double beginZ, endZ;//shift box on z axis
    double targetX, targetY, targetZ;
    
    /**BOMBS AND TARGETS**/
    ArrayList<Bomb> bombs; 
    ListIterator<Bomb> bi; 
    ArrayList<Target> targets;
    ListIterator<Target> ti; 
    int targetAmount;
    
    /** GRAVITY AND NEIGHBORS**/
    double squareRadius = 10000;//sphere of neighbors to look around
    
    /**MULTIPLE KEY PRESS BOOLEANS**/
    boolean forward, backward,strafingLeft, strafingRight, strafingUp, strafingDown, turningLeft, turningRight, turningUp, turningDown;
    
    /**GAME CONSTRUCTOR**/
    public Game() {
        //BOMBS AND TARGETS SETUP
        bombs = new ArrayList<Bomb>();
        targets = new ArrayList<Target>();
        
        //DELTA TIME SETUP
        lastTime = System.nanoTime();
        theta = 0;        
        
        //MOVEMENT SETUP
        forward = false;
        backward = false;
        strafingLeft = false;
        strafingRight = false;
        strafingUp = false;
        strafingDown = false;
        turningLeft = false;
        turningRight = false; 
        turningUp = false;
        turningDown = false; 
        
        //SETUP CAMERA
        //The camera is located at the origin, looking along the positive z-axis, with y-up
        //                  initial location       which axis out                 which axis up
        camera = new Camera(0,0,-5100,                 0,0,1,                         0,1,0);
        
        //BOX SETUP
        boxWidth = 500;
        boxHeight = 300;
        boxLength = 500;        
        beginZ = boxLength - boxLength - 5000;
        endZ = beginZ - boxLength;        
        targetX = boxWidth - 20;
        targetY = boxHeight - 20;
        
        //CREATE TARGETS
        targetAmount = 100;
        for(int i = 0; i < targetAmount; i++){
            //positions
            double posX = Math.random()*(targetX + targetX )-targetX;
            double posY = Math.random()*(targetY+targetY)-targetY;
            double posZ= ( Math.random()*(beginZ - endZ) + endZ);           
            //velocities
            double velx = Math.random()*0.1;
            double vely = Math.random()*0.1;
            double velz = Math.random()*0.1;
            //add to list
            targets.add(new Target(posX,posY,posZ, velx , vely, velz));
        }
    }
    
    /**GAME UPDATE FUNCTION**/
    public synchronized void update () {
        //DELTA TIME
        long time = System.nanoTime() - lastTime;
        lastTime = System.nanoTime();
        theta += time/10000000000.0;
        
        //CREATE KD TREE
        KD_Tree tree = new KD_Tree(targets);
        
        
        /**BOMBS**/
        bi = bombs.listIterator();        
        //check for first element
        if(bi.hasNext())
        {
            bi.next();
            bi.previous().update(time);
        }        
        //FOR ALL BOMBS
        while (bi.hasNext()) {
            Bomb b = bi.next();
            b.update(time);
            
            LinkedList<Target> neighbors = tree.neighbors(b, squareRadius);
            
            /**CHECK COLLISIONS**/
            //start at beginning of neighbors linked list 
            ListIterator<Target> bj = neighbors.listIterator();
            
            while(bj.hasNext()) {
                Target t = bj.next();
                
                //distance formula
                double distance = sqr(b.posx - t.posx) + 
                                  sqr(b.posy - t.posy) +
                                  sqr(b.posz - t.posz); 
                if(distance < 200 ){
                    t.isDead = true;
                    b.isDead = true;
                }
            }
            
            //remove if necessary
            if(b.dead())
            {
                //Use iterator remove
                bi.remove();
            }
            
        }
        
        /**TARGETS**/
        ti = targets.listIterator();        
        //check so first element in array list can be updated
        if(ti.hasNext())
        {
            ti.next();
            ti.previous().update(time);
        }        
        while (ti.hasNext()) {
            Target t = ti.next();
            t.update(time);

            //remove if necessary
            if(t.dead())
            {
                //Use iterator remove
                ti.remove();
            }
            
        }
        
        /**CAMERA MOVEMENT**/        
        if(forward) {
            camera.forward();
        }
        if(backward) {
            camera.back();
        }
        if(strafingLeft) {
            camera.sLeft();
        }
        if(strafingRight) {
            camera.sRight();
        }
        if(turningLeft) {
            camera.left();
        }
        if(turningRight) {
            camera.right();
        }
        if(turningUp) {
            camera.lookUp();
        }
        if(turningDown) {
            camera.lookDown();
        }
             
    }
    
    /**GAME DISPLAY FUNCTION**/
    public synchronized void display (GLAutoDrawable gld)
    {        
        /**----------SETUP OPENGL---------**/
        final GL2 gl = gld.getGL().getGL2();
        // Clear the buffer, need to do both the color and the depth buffers
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        // Load the identity into the Modelview matrix
        gl.glLoadIdentity();
        // Setup the camera.  The camera is located at the origin, looking along the positive z-axis, with y-up
        camera.setLookAt(glu); 
        /**----------SETUP LIGHT #1---------**/
        // set the position and diffuse/ambient terms of the light
        //              bit to right,  bit up,  behind you   ambient?
        float [] pos = {1,             1,       -1,          0};
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, pos, 0);
        float [] diffuse = {0.7f, 0.7f, 0.7f, 1.0f};
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
        float [] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, ambient, 0);
        float [] specular = {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_SPECULAR, specular, 0);     
        
        /**----------QUADS---------**/
        gl.glBegin(GL2GL3.GL_QUADS);
        //-----front-----
        setMaterial(gld, 1.0f, 0.0f, 0.0f);
        gl.glNormal3d(0,0,-1);
        gl.glVertex3d(-boxWidth, -boxHeight, beginZ);
        gl.glVertex3d(-boxWidth, boxHeight, beginZ);
        gl.glVertex3d(boxWidth, boxHeight, beginZ);
        gl.glVertex3d(boxWidth, -boxHeight, beginZ);
        //-----back-----
        setMaterial(gld, 0.0f, 1.0f, 0.0f);
        gl.glNormal3d(0,0,-1);
        gl.glVertex3d(boxWidth, -boxHeight, endZ);
        gl.glVertex3d(boxWidth, boxHeight, endZ);
        gl.glVertex3d(-boxWidth, boxHeight, endZ);
        gl.glVertex3d(-boxWidth, -boxHeight, endZ);
        //-----left-----
        setMaterial(gld, 1.0f, 1.0f, 0.0f);
        gl.glNormal3d(0,0,-1);
        gl.glVertex3d(boxWidth, -boxHeight,  beginZ); 
        gl.glVertex3d(boxWidth,  boxHeight,   beginZ);
        gl.glVertex3d(boxWidth,  boxHeight,  endZ);
        gl.glVertex3d(boxWidth, -boxHeight, endZ);
        //-----right-----
        setMaterial(gld, 0.0f, 0.0f, 1.0f);
        gl.glNormal3d(0,0,-1);
        gl.glVertex3d(-boxWidth, -boxHeight, endZ);
        gl.glVertex3d(-boxWidth,  boxHeight,  endZ);
        gl.glVertex3d(-boxWidth,  boxHeight,   beginZ);
        gl.glVertex3d(-boxWidth, -boxHeight,  beginZ); 
        //-----top-----
        setMaterial(gld, 0.0f, 1.0f, 1.0f);
        gl.glNormal3d(0,0,-1);
        gl.glVertex3d(-boxWidth, boxHeight, endZ);
        gl.glVertex3d( boxWidth, boxHeight,  endZ);
        gl.glVertex3d( boxWidth, boxHeight,   beginZ);
        gl.glVertex3d(-boxWidth, boxHeight,  beginZ); 
        //-----bottom-----
        setMaterial(gld, 1.0f, 0.0f, 1.0f);
        gl.glNormal3d(0,0,-1);
        gl.glVertex3d(-boxWidth, -boxHeight,  beginZ);
        gl.glVertex3d( boxWidth, -boxHeight,   beginZ);
        gl.glVertex3d( boxWidth, -boxHeight,  endZ);
        gl.glVertex3d(-boxWidth, -boxHeight, endZ);   

        /**BOMBS**/
        bi = bombs.listIterator();        
        //check so first element in array list can be displayed
        if(bi.hasNext())
        {
            bi.next();
            bi.previous().draw(gld);
        }        
        while (bi.hasNext()) {
            Bomb b = bi.next();
            b.draw(gld);
        }
             
        /**TARGETS**/
        ti = targets.listIterator();        
        //check so first element in array list can be displayed
        if(ti.hasNext())
        {
            ti.next();
            ti.previous().draw(gld);
        }        
        while (ti.hasNext()) {
            Target t = ti.next();
            t.draw(gld);
        }
        gl.glEnd();
    }
    
    /**FUNCTION USED TO SET DIFFUSE/AMBIENT AND COLOR FOR OPENGL**/
    public void setMaterial(GLAutoDrawable gld, float r, float g, float b)
    {
        final GL2 gl = gld.getGL().getGL2();
        
        float [] ad = {r, g, b, 1.0f};
        float [] s = {1.0f, 1.0f, 1.0f, 1.0f};
        
        //whatever material you set at the moment you draw is the one you use
        //similar to beginFill(color)
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, ad, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, s, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 64.0f); 
    } 

    /**KEY DOWN HANDLER**/
    public synchronized void keyPressed (KeyEvent e)
    {
        /**MOVEMENT**/
        if (e.getKeyCode() == KeyEvent.VK_W) {
            forward = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            backward = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            strafingLeft = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_D) {
            strafingRight = true;        
            }
        
        /**LOOKING**/
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            turningLeft = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            turningRight = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            turningUp = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            turningDown = true;
        }
        
        /**SHOOTING**/
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            //find direction camera is looking
            double dx = camera.centerX - camera.eyeX, dy = camera.centerY - camera.eyeY, dz = camera.centerZ - camera.eyeZ;
            //create unit vector
            double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
            dx /= m;
            dy /= m;
            dz /= m;            
            //calculate position to place bomb
            double posx = camera.centerX + dx*30;
            double posy = camera.centerY + dy*30;
            double posz = camera.centerZ + dz*30;            
            //velocity
            double velx = dx*300;
            double vely = dy*300;
            double velz = dz*300;
            //add            
            bombs.add(new Bomb(posx, posy, posz, velx, vely, velz)); 
         }

    }

    /**KEY UP HANDLER**/
    public void keyReleased (KeyEvent e)
    {
        /**MOVEMENT**/
        if (e.getKeyCode() == KeyEvent.VK_W) {
            forward = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            backward = false;
        }
        if(e.getKeyCode() == KeyEvent.VK_A) {
            strafingLeft = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_D) {
            strafingRight = false;        
            }
        
        /**LOOKING**/
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            turningLeft = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            turningRight = false;
        }  
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            turningUp = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            turningDown = false;
        }
    }
    
    public void reshape (GLAutoDrawable gld, int x, int y, int width, int height)
    {
        GL gl = gld.getGL();
        winWidth = width;
        winHeight = height;
        gl.glViewport(0,0, width, height);
    }

    public void init (GLAutoDrawable gld)
    {
        glu = new GLU();
        glut = new GLUT();
        final GL2 gl = gld.getGL().getGL2();
        gl.glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
        // setup the projection matrix
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        //                 field of view  aspect ratio  close clipping plane   far clipping plane
        //                                1.77w 1.33sq
        glu.gluPerspective(60.0,          1.33,         0.001,                 1000);
        
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glEnable(GLLightingFunc.GL_NORMALIZE); // automatically normalizes stuff
        gl.glEnable(GL.GL_CULL_FACE); // cull back faces [only draw things facing you]
        gl.glEnable(GL.GL_DEPTH_TEST); // turn on z-buffer
        gl.glEnable(GLLightingFunc.GL_LIGHTING); // turn on lighting
        gl.glEnable(GLLightingFunc.GL_LIGHT0); // turn on light [if more lights, you have to enable them here
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH); // smooth normals
    }
    public void init() {
        setLayout(new FlowLayout());
        // create a gl drawing canvas
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);
        canvas.setPreferredSize(new Dimension(winWidth, winHeight));        
        // add gl event listener
        canvas.addGLEventListener(this);
        add(canvas);
        setSize(winWidth, winHeight);
        canvas.addKeyListener(this);
        // add the canvas to the frame
        animator = new FPSAnimator(canvas, 60);
        updateThread = new Thread(new Runnable() {
        public void run() {
            while(true) {
            update();
            /**MULTI THREADING? (RUNS FASTER)**/
            try
            {
                Thread.sleep(30);
            }
            catch (InterruptedException exception)
            {
                exception.printStackTrace();
            }
            }
        }
        });
    }
    public void start() {
        animator.start();
        updateThread.start();
    } 
    
    public void stop() {
        animator.stop();
    }

    /**FUNCTION TO SQUARE INPUT**/
    double sqr( double x) { return x*x;}
 
    /**INNER CLASS BOMB**/
    public class Bomb {
        /**Class Member Variables**/
        //positions, velocities, and forces using f = ma
        double posx, posy, posz, velx, vely, velz;
        float color_r, color_g, color_b;
        double size;
        double bounceDistance;
        boolean isDead;
        
        /**BOID CONSTRUCTOR**/
        Bomb(double posx, double posy, double posz, double velx, double vely, double velz) {
            //initiate variables
            this.posx = posx;
            this.posy = posy;
            this.posz = posz;
            this.velx = velx;
            this.vely = vely;
            this.velz = velz;
            //size
            size = 5.0;
            bounceDistance = size/2;
            //color
            color_r = (float)Math.random();
            color_g = (float)Math.random();
            color_b = (float)Math.random();
            //dead boolean
            isDead = false;
        }
        
        /**UPDATE METHOD**/
        public void update(long time) 
        {
            //shrink size according to delta time
            if(size > 0)
                size -= (double)time/1000000000.0;
            if(size <= 0) {
                isDead = true;
            }
            
            bounce();
            
            //update position
            posx = posx + velx*time/500000000.0;
            posy = posy + vely*time/500000000.0;
            posz = posz + velz*time/500000000.0;
        }
           
        /**DRAW FOR SINGLE BOMB**/
        public void draw(GLAutoDrawable gld) {
            /**----------SETUP OPENGL---------**/
            final GL2 gl = gld.getGL().getGL2();
            gl.glPushMatrix();

            //draw bomb
            gl.glTranslated(posx, posy, posz);
            setMaterial(gld, color_r, color_g, color_b);
            glut.glutSolidSphere(size, 50, 50);
            
            gl.glPopMatrix(); 
            
            gl.glEnd();
        }
        
        /**GETTER FOR isDead BOOLEAN**/
        public boolean dead() 
        {
            return isDead;
        }
        
        /**FUNCTION TO CALCUALTE BOUNCING OFF WALLS**/
        public void bounce() {
            //front and back walls
            if( posz >= beginZ){
                 velz *= -0.9;
                 posz = beginZ -  bounceDistance;
            }
            else if( posz <= endZ){
                 velz *= -0.9;
                 posz = endZ +  bounceDistance;
            }
            
            //right and left walls
            if( posx >= boxWidth )
            {
                 velx *= -0.9;
                 posx = boxWidth -  bounceDistance;
            }
            else if( posx <= -boxWidth) {
                 velx *= -0.9;
                 posx = -boxWidth +  bounceDistance;
            }
            
            //top and bottom walls
            if( posy >= boxHeight )
            {
                 vely *= -0.9;
                 posy = boxHeight -  bounceDistance;
            }
            else if( posy <= -boxHeight) {
                 vely *= -0.9;
                 posy = -boxHeight +  bounceDistance;
            }
        }
    }
    
    /**INNER CLASS TARGET**/
    public class Target {
        /**Class Member Variables**/
        //positions, velocities, and forces using f = ma
        double posx, posy, posz, velx, vely, velz;
        float color_r, color_g, color_b;
        double size;
        double bounceDistance;
        boolean isDead;
        
        /**TARGET CONSTRUCTOR**/
        Target(double posx, double posy, double posz, double velx, double vely, double velz) {
            //initiate variables
            this.posx = posx;
            this.posy = posy;
            this.posz = posz;
            this.velx = velx;
            this.vely = vely;
            this.velz = velz;
            
            size = 5.0;
            bounceDistance = size/2;
            
            color_r = (float)Math.random();
            color_g = (float)Math.random();
            color_b = (float)Math.random();
            
            isDead = false;
        }
        
        /**UPDATE METHOD**/
        public void update(long time) 
        {
            //update position
            posx = posx + velx*time/2500000.0;
            posy = posy + vely*time/2500000.0;
            posz = posz + velz*time/2500000.0;
            
            bounce();
        }
           
        /**DRAW FOR SINGLE TARGET**/
        public void draw(GLAutoDrawable gld) {
            /**----------SETUP OPENGL---------**/
            final GL2 gl = gld.getGL().getGL2();
            gl.glPushMatrix();            
            gl.glTranslated(posx, posy, posz);
            gl.glRotated(360*theta, 0, 0, 1);
            setMaterial(gld, color_r, color_g, color_b);
            glut.glutSolidCube((float)size);
            gl.glPopMatrix();             
            gl.glEnd();
        }
        
        /**GETTER FOR isDead BOOLEAN**/
        public boolean dead() 
        {
            return isDead;
        }
        
        /**FUNCTION TO CALCUALTE BOUNCING OFF WALLS**/
        public void bounce() {
            //front and back walls
            if( posz >= beginZ){
                 velz *= -0.9;
                 posz = beginZ -  bounceDistance;
            }
            else if( posz <= endZ){
                 velz *= -0.9;
                 posz = endZ +  bounceDistance;
            }
            
            //right and left walls
            if( posx >= boxWidth )
            {
                 velx *= -0.9;
                 posx = boxWidth -  bounceDistance;
            }
            else if( posx <= -boxWidth) {
                 velx *= -0.9;
                 posx = -boxWidth +  bounceDistance;
            }
            
            //top and bottom walls
            if( posy >= boxHeight )
            {
                 vely *= -0.9;
                 posy = boxHeight -  bounceDistance;
            }
            else if( posy <= -boxHeight) {
                 vely *= -0.9;
                 posy = -boxHeight +  bounceDistance;
            }
        }
    }
    
    /**INNER CLASS CAMERA**/
    public class Camera {
        double eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ;
        double ctheta;
        double speed;
        
        Camera(double eyeX, double eyeY, double eyeZ, double centerX, double centerY, double centerZ, double upX, double upY, double upZ) {
            //initialize variables
            this.eyeX = eyeX;
            this.eyeY = eyeY;
            this.eyeZ = eyeZ;
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.upX = upX;
            this.upY = upY; 
            this.upZ = upZ;
            
            //front and back movement speed
            speed = 0.3;
            //rotation speed
            ctheta = 0.0487266463;
        }
            
        void setLookAt(GLU glu) {
        glu.gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        }
            
        void forward() {
            double dx = centerX - eyeX, dy = centerY - eyeY, dz = centerZ - eyeZ;
            double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
            dx /= m*speed;
            dy /= m*speed;
            dz /= m*speed;
            
            centerX += dx;
            centerY += dy;
            centerZ += dz;
            eyeX += dx;
            eyeY += dy;
            eyeZ += dz;
        }
            
        void back() {
            double dx = centerX - eyeX, dy = centerY - eyeY, dz = centerZ - eyeZ;
            double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
            dx /= m*speed;
            dy /= m*speed;
            dz /= m*speed;
                    
            centerX -= dx;
            centerY -= dy;
            centerZ -= dz;
            eyeX -= dx;
            eyeY -= dy;
            eyeZ -= dz;
        }
        
        void sLeft() {
            double dx = centerX - eyeX, dy = centerY - eyeY, dz = centerZ - eyeZ;
            double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
            dx /= m*speed;
            dy /= m*speed;
            dz /= m*speed;

            centerX += dz;
            centerY += dy;
            centerZ -= dx;
            eyeX += dz;
            eyeY += dy;
            eyeZ -= dx;
        }
            
        void sRight() {
            double dx = centerX - eyeX, dy = centerY - eyeY, dz = centerZ - eyeZ;
            double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
            dx /= m*speed;
            dy /= m*speed;
            dz /= m*speed;
                    
            centerX -= dz;
            centerY -= dy;
            centerZ += dx;
            eyeX -= dz;
            eyeY -= dy;
            eyeZ += dx;
        }
            
        void left() {
            double dx = centerX - eyeX, dy = centerY - eyeY, dz = centerZ - eyeZ;
            double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
            dx /= m;
            dy /= m;
            dz /= m;
            double nx = Math.cos(ctheta)*dx + Math.sin(ctheta)*dz;
            double ny = dy;
            double nz = -Math.sin(ctheta)*dx + Math.cos(ctheta)*dz;
            
            centerX = eyeX+nx;
            centerY = eyeY+ny;
            centerZ = eyeZ+nz;
        }

        void right() {
            double dx = centerX - eyeX, dy = centerY - eyeY, dz = centerZ - eyeZ;
            double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
            dx /= m;
            dy /= m;
            dz /= m;
            double nx = Math.cos(ctheta)*dx - Math.sin(ctheta)*dz;
            double ny = dy;
            double nz = Math.sin(ctheta)*dx + Math.cos(ctheta)*dz;
            
            centerX = eyeX+nx;
            centerY = eyeY+ny;
            centerZ = eyeZ+nz;
        }
        
        double [][] axisAngleToMatrix(double upX1, double upY1, double upZ1, double theta1) {
            double [][] m = new double [3][3];
            m[0][0] = Math.cos(theta1) + (upX1*upX1)*(1-Math.cos(theta1));
            m[0][1] = upX1*upY1*(1-Math.cos(theta1)) - upZ1*Math.sin(theta1);
            m[0][2] = upX1*upZ1*(1-Math.cos(theta1)) + upY1*Math.sin(theta1);
            
            m[1][0] = upY1*upX1*(1-Math.cos(theta1)) + upZ1*Math.sin(theta1);
            m[1][1] = Math.cos(theta1) + upY1*upY1 * (1-Math.cos(theta1));
            m[1][2] = upY1*upZ1*(1-Math.cos(theta1)) - upX1*Math.sin(theta1);
            
            m[2][0] = upZ1*upX1*(1-Math.cos(theta1)) -upY1*Math.sin(theta1);
            m[2][1] = upZ1*upY1*(1-Math.cos(theta1)) + upX1*Math.sin(theta1);
            m[2][2] = Math.cos(theta1) + upZ1*upZ1*(1-Math.cos(theta1));
            return m;
        }
        
        double []  matrixVectorMultiply(double [][] m, double upX1, double upY1, double upZ1) {
            double [] x = new double [3];
            x[0] = m[0][0] * upX1 + m[0][1] * upY1 + m[0][2] * upZ1;
            x[1] = m[1][0] * upX1 + m[1][1] * upY1 + m[1][2] * upZ1;
            x[2] = m[2][0] * upX1 + m[2][1] * upY1 + m[2][2] * upZ1;
            return x;
        }
        
        double []  cross(double upX1, double upY1, double upZ1, double vx, double vy, double vz) {
            double [] x = new double[3];
            x[0] = upY1*vz - upZ1*vy;
            x[1] = upZ1*vx - upX1*vz;
            x[2] = upX1*vy - upY1*vx;
            return x;
        }
        
        void lookDown() { 
            double dx = centerX - eyeX, dy = centerY - eyeY, dz = centerZ - eyeZ;
            double m = Math.sqrt(sqr(dx) + sqr(dy) + sqr(dz));
            dx /= m;
            dy /= m;
            dz /= m;

            double [] l = cross(upX, upY, upZ, dx, dy, dz);
            double [][] mat = axisAngleToMatrix(l[0], l[1], l[2], ctheta);
            double [] n = matrixVectorMultiply(mat, dx, dy, dz);
            double [] u = matrixVectorMultiply(mat, upX, upY, upZ);
//            double [] u = cross(n[0], n[1], n[2], l[0], l[1], l[2]);
            upX = u[0];
            upY = u[1];
            upZ = u[2];
            centerX = eyeX + n[0];
            centerY = eyeY + n[1];
            centerZ = eyeZ + n[2];
        }

        void lookUp() { 
            double dx = centerX - eyeX, dy = centerY - eyeY, dz = centerZ - eyeZ;
            double m = Math.sqrt(sqr(dx) + sqr(dy) + sqr(dz));
            dx /= m;
            dy /= m;
            dz /= m;

            double [] l = cross(upX, upY, upZ, dx, dy, dz);
            double [][] mat = axisAngleToMatrix(l[0], l[1], l[2], -ctheta);
            double [] n = matrixVectorMultiply(mat, dx, dy, dz);
            double [] u = matrixVectorMultiply(mat, upX, upY, upZ);
//            double [] u = cross(n[0], n[1], n[2], l[0], l[1], l[2]);
            upX = u[0];
            upY = u[1];
            upZ = u[2];
            centerX = eyeX + n[0];
            centerY = eyeY + n[1];
            centerZ = eyeZ + n[2];
        }
    }
    
    public void keyTyped (KeyEvent e)
    {        
    }
    
    public void dispose (GLAutoDrawable arg0)
    {       
    }
    
    public void displayChanged (GLAutoDrawable gld, boolean arg1, boolean arg2)
    {
    }
}