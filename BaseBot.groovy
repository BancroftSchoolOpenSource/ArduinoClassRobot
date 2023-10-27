import java.nio.file.Files
import java.nio.file.Paths

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import eu.mihosoft.vrl.v3d.FileUtil
import eu.mihosoft.vrl.v3d.RoundedCube
import eu.mihosoft.vrl.v3d.Sphere
import eu.mihosoft.vrl.v3d.Transform
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter
import javafx.scene.paint.Color

double caseRounding = 4

class BoardMaker{
	static double radius = 1
	static double insertionDepthOfWii = 7.1
	static double wiiCenterOffset=2.2
	static double wiiConnectorThickness = 7.40
	static double wiiConnectorWidth = 12.21
	static double wiiInsetFromEdge = 12.42+(wiiConnectorWidth/2)
	static double boardX =67.35
	static double boardY =75.0
	static double boardZ =1.67
	static double cutoutRadius=4
	static double cutoutDepth = 7.3
	static double ioKaX =63.7
	static double ioKaY =48.79
	static double ioKaZ =17.67
	static double lowerKeepaway = 6.3
	static double holeCornerInset = 1.8+1.5
	static double boardConnects = 2.3
	static double usbThickness = 6.9
	static double wireHeight = 5
	static double wireRadius = 1.5
	static double negativeWireOffset = 3-3.5
	static double positiveWireOffset = 7+3.5
	static double caseOutSet = 4
	static double powerKeepawayOffset=56.6
	static double usbHeight=11.06
	static LengthParameter	printerOffset 		= new LengthParameter("Printer Offset",0.6,[1.75,0])
	double caseRounding = 4
	def makeWiiConnector(){
		CSG wiiConnect = new RoundedCube(	wiiConnectorWidth,// X dimention
					wiiConnectorThickness,// Y dimention
					8.11+(radius)//  Z dimention
					).cornerRadius(radius)// sets the radius of the corner
					.toCSG()
					.toZMin()
					.movez(-radius)
		
		CSG cutout = new RoundedCube(	5,// X dimention
					1.58+radius,// Y dimention
					8.11+(radius*2)//  Z dimention
					)
					.cornerRadius(radius)// sets the radius of the corner
					.toCSG()
					.toZMin()
					.movez(-radius)
					.toYMax()
					.movey(radius)
					.movey(wiiConnect.getMaxY())
		CSG wiiBody = new Cube(	20.27,// X dimention
					13,// Y dimention
					23.4//  Z dimention
					)
					.toCSG()
					.toZMax()
		CSG wiiClip = new Cube(	17.9,// X dimention
					6.62,// Y dimention
					5//  Z dimention
					)
					.toCSG()
					.toZMax()	
					.toYMax()
					.movey(wiiBody.getMinY())	
		CSG wiiNotch = new Cube(	14.75,// X dimention
					3.5,// Y dimention
					3.5//  Z dimention
					)
					.toCSG()
					.toZMin()
					.toYMin()
					.movez(wiiConnect.getMaxZ()-5)
					.movey(wiiConnect.getMinY()+1.5)				
		wiiConnect=wiiConnect
				.difference(cutout)		
				.union([wiiBody,
				wiiClip,
				wiiNotch])
				
				
		return wiiConnect	
	}
	def makeBoard(){
		CSG wiiConnect = makeWiiConnector()	
		wiiConnect=wiiConnect
				.toZMax()
				.movez(insertionDepthOfWii)
				.movey(wiiConnectorThickness/2-wiiCenterOffset)		
				.rotx(-90)
				.rotz(180)
				.movex(wiiInsetFromEdge)
				.movez(-boardZ)
							
		CSG mainBoard = new Cube(boardX+boardConnects*2,boardY+boardConnects,boardZ).toCSG()
						.toXMin()
						.toYMin()
						.toZMax()
						.move(-boardConnects,-boardConnects,0)
		
		CSG mainBoardCutout = new RoundedCube(33.0-1,cutoutDepth+cutoutRadius,boardZ+cutoutRadius*2)
						.cornerRadius(cutoutRadius)
						.toCSG()
						.toYMax()
						.toZMax()	
						.movez(cutoutRadius)
						.movey(mainBoard.getMaxY()+cutoutRadius)
						.movex(boardX/2)
		mainBoard=mainBoard.difference(mainBoardCutout)
		
		CSG antenna = new Cube(21.85,7,13.69+boardZ+lowerKeepaway+1).toCSG()
					.toYMax()
					.toZMin()
					.movey(-ioKaY)
					.movex(ioKaX/2)
		CSG IOkeepaway = new Cube(ioKaX,ioKaY,ioKaZ+boardZ+lowerKeepaway).toCSG()
						.toXMin()
						.toYMax()
						.toZMin()
						.union(antenna)
						.movez(-(boardZ+lowerKeepaway))
						.movey(-cutoutDepth+boardY)
						.movex(1.8)
		CSG fusekeepaway = new Cube(20-0.12,9,ioKaZ+boardZ+lowerKeepaway+10).toCSG()
						.toXMin()
						.toYMin()
						.toZMin()
						.movez(-(boardZ+lowerKeepaway))
						.movey(IOkeepaway.getMinY()-1.9)
						.movex(1.63)
		CSG electronicsKeepaway = new Cube(30,19.4,3.2+(boardZ+lowerKeepaway)).toCSG()
						.toXMin()
						.toYMin()
						.toZMin()
						.movez(-(boardZ+lowerKeepaway))
						.movex(wiiConnect.getMaxX()-7.5)
						.difference(wiiConnect.intersect(wiiConnect.getBoundingBox().toYMin()))
		CSG switchkeepaway = new Cube(20,10.6,ioKaZ+boardZ+lowerKeepaway+10).toCSG()
						.toXMax()
						.toYMin()
						.toZMin()
						.movez(-(boardZ+lowerKeepaway))
						.movey(6.81)
						.movex(boardX-1.63)
		CSG holeLower = new Cylinder(1.25,1.25,ioKaZ,(int)20).toCSG()
					.movez(-ioKaZ)
		CSG holeUpper = new Cylinder(1.5,1.5,ioKaZ,(int)10).toCSG()
		CSG holeHead = new Cylinder(3.1,3.1,ioKaZ,(int)20).toCSG()
					.movez(7)			
		def hole = holeUpper.union([holeLower,holeHead])
		mainBoard=mainBoard
			.union(hole.move(holeCornerInset,holeCornerInset,0))
			.union(hole.move(holeCornerInset,boardY-holeCornerInset,0))
			.union(hole.move(boardX-holeCornerInset,boardY-holeCornerInset,0))
			.union(hole.move(boardX-holeCornerInset,holeCornerInset,0))
		def WireActualPartLine= wireHeight-caseRounding
		CSG wire = new Cylinder(wireRadius,40).toCSG()
				.movez(-wireRadius)
				.rotx(-90)
				.movez(WireActualPartLine)
		CSG wirekeepaway = new Cube(positiveWireOffset+wireRadius*6,boardConnects,wireHeight).toCSG()
							.toXMax()
							.toZMin()
							.toYMax()
							.movex(wireRadius+3.5)
		CSG powerkeepaway = new Cube(17.84,19.79,10.43+boardZ+lowerKeepaway).toCSG()
						.toXMax()
						.movex(3.5)
						.toYMin()
						.toZMin()
						.movez(-(boardZ+lowerKeepaway))
						.union(wire.movex(-negativeWireOffset))
						.union(wire.movex(-positiveWireOffset))
						.union(wirekeepaway)
						.movex(powerKeepawayOffset)
		CSG usbCord = new Cylinder(2.0,40).toCSG()
						.rotx(90)
						.movez(usbThickness/2)
		CSG usbCordkeepaway = new RoundedCube(6,1,usbThickness)
						.cornerRadius(1)
						.toCSG()
						.toYMax()
						.toZMin()
						.movey(27.75)
						
		CSG usbkeepaway = new RoundedCube(11.1,20.73,usbThickness)
						.cornerRadius(1)
						.toCSG()
						.toYMin()
						.toZMin()
						.union(usbCordkeepaway)
						.hull()
						.union(usbCord)
						.movey(-cutoutDepth+boardY)
						.movex(boardX/2)
						.movez(usbHeight)
		def lightPipe = new Cylinder(1.5,100).toCSG()	
		def v5Power = lightPipe.move(41.21,2.8,0)
		def fused = lightPipe.move(36.26,10,0)
		def vcc = lightPipe.move(44.65,8.6,0)
		def screwTerm = new Cube(4,4,100).toCSG().toZMin().union(new Cube(1.5,1.5,100).toCSG().toZMin().rotx(-90))
		def point1 = 0.1*25.4
		screwTerm =screwTerm .union(screwTerm.movex(point1))
					.movez(WireActualPartLine)
					.movey(point1)
					.movex(powerKeepawayOffset-positiveWireOffset*0.5-point1/2)
		
		//return electronicsKeepaway
		return [wiiConnect,mainBoard,IOkeepaway,switchkeepaway,powerkeepaway,usbkeepaway,fusekeepaway,electronicsKeepaway,
		v5Power,
		fused,
		vcc,
		screwTerm
		]
	}
	def makeCase(){
		def board =makeBoard()
		
		double frontCaseDepth = -cutoutDepth+boardY-ioKaY-caseRounding+3
		double lowerCaseDepth = Math.abs(board[0].getMinZ())
		if(lowerCaseDepth<(lowerKeepaway+3)){
			lowerCaseDepth=lowerKeepaway+3
		}
		CSG wirekeepaway = new RoundedCube(positiveWireOffset+wireRadius*8,caseOutSet-boardConnects,wireHeight+4)
							.toCSG()
							.toXMax()
							.toZMin()
							.toYMin()
							.movez(-caseRounding*2)
							.movex(wireRadius*3)	
							.movey(-caseOutSet)
							.movex(powerKeepawayOffset)	
		double LugX = boardX+(boardConnects*2)+caseOutSet*2
		CSG basicLug = new RoundedCube(LugX,frontCaseDepth,lowerCaseDepth)
						.cornerRadius(caseRounding)
						.toCSG()
						.toZMax()
						.toYMin()
						.movey(-caseOutSet)
						.toXMin()
						.movex(-caseOutSet-boardConnects)
						
					
		CSG fullBoard = CSG.unionAll(board)
		
						
						
		
		double backeOfCaseY = boardY-	cutoutDepth+printerOffset.getMM()*2
		CSG usbkeepaway = new RoundedCube(13+caseRounding*2,frontCaseDepth,usbHeight+caseRounding*2+usbThickness/2)
							.cornerRadius(caseRounding)
							.toCSG()
							.toZMin()
							.movez(-caseRounding*2)
							.toYMin()
							.movex(boardX/2)
							
		
		CSG backBottom = basicLug
						.toYMin()
						.movey(backeOfCaseY)
						.union(basicLug)
						.hull()
						.union(usbkeepaway.movey(backeOfCaseY))
						.union(	wirekeepaway)	
						.difference(fullBoard)
		double vexGrid = 1.0/2.0*25.4
//		CSG vexMount = Vitamins.get( "vexFlatSheet","Aluminum 1x5")		
//						.intersect(new Cube(vexGrid*7.5).toCSG())
//						.rotz(-90)
//						.movey(	-caseOutSet+caseRounding+vexGrid/2)	
//						.movex(-caseOutSet+2-boardConnects-vexGrid/2)
//						.movez(		backBottom.getMinZ())	
//		vexMount=vexMount.movey(vexGrid*2)
//					.union(vexMount)
//		CSG vexMountB = vexMount.movex(vexGrid*7)
//		def vexAttach = vexMount.union(vexMountB)
//						.hull()
//						.difference([vexMount.hull(),vexMountB.hull()])
//						
//		def allvexbits = CSG.unionAll([vexMountB,
//						vexMount,
//						vexAttach
//						])
//						.toYMin()
//						.movey(backBottom.getMinY()+caseRounding)	
//		backBottom=	backBottom	
//			.union(allvexbits)
		println "Performing keepaway opperation ..."
		def fullBoardMink =CSG.unionAll(fullBoard.minkowski(new Cube(printerOffset.getMM()).toCSG()))
		def backBottomMink =CSG.unionAll(backBottom.minkowski(new Cube(printerOffset.getMM()).toCSG()))
		println "keepaway Done!"
		double caseheight = 20
		def rounding = makeRoundedCyl(
                      		basicLug.getTotalX()*0.60, // Radius at the top
                      		basicLug.getTotalY(), // Height
                      		caseRounding,
                      		(int)80 //resolution
                      		)
                      		.rotx(90)
                      		.movey(basicLug.getMinY())
                      		.movex(boardX/2)
                      		.toZMax()
                      		.movez(caseheight+caseRounding)
		CSG frontTop = basicLug.union(basicLug.toZMax().movez(	caseheight))
						.hull()
						.toZMin()
						.movez(-caseRounding*2)
						.intersect(rounding)
						//.difference(fullBoardMink)
						//.difference(backBottomMink)	
		CSG backTop = basicLug.union(basicLug.toZMax().movez(	caseheight))
						.hull()
						.toZMin()
						.movez(-caseRounding*2)
						.intersect(rounding)
						.toYMin()
						.movey(backeOfCaseY)
						//.difference(fullBoardMink)	
						//.difference(backBottomMink)	
		CSG crossbar = new RoundedCube(30,backeOfCaseY+(basicLug.getTotalY())-basicLug.getMinY(),caseRounding*2)
						.cornerRadius(caseRounding)
						.toCSG()
						.toYMin()
						.movex(boardX/2)
						.movey(basicLug.getMinY())
						.toZMax()
						.movez(frontTop.getMaxZ())
						
		CSG topPlate  = frontTop.union(backTop)
							.union(crossbar)
							.difference(fullBoardMink)
							.difference(backBottomMink)
		CSG bottom = backBottom			
//		bottom.setManufacturing({ toMfg ->
//			return toMfg
//					.toXMin()
//					.toYMin()
//					.toZMin()
//		})	
//		topPlate.setManufacturing({ toMfg ->
//			return toMfg
//					.toXMin()
//					.toYMin()
//					.rotx(-180)
//					.toZMin()
//		})	
		bottom.setName("CaseBottom")
		topPlate.setName("CaseTop")
		def caseParts = [bottom,topPlate]
		return caseParts
		//board.addAll(caseParts)
		//return board
	}
	def makeRoundedCyl(def rad,def height, def corner,def resolution){
		def minHeight = height-corner*2
		def cylParts =[]
		def divisor = 6
		for(int i=0;i<divisor;i++){
			def radInc = rad-corner+Math.sin(Math.PI/2*((double)i/(double)divisor))*corner
			def heightInc = (Math.cos(Math.PI/2*((double)i/(double)(divisor)))*corner)
			cylParts.add(
				new Cylinder(radInc, // Radius at the bottom
                      		radInc, // Radius at the top
                      		heightInc*2+minHeight, // Height
                      		(int)resolution //resolution
                      		).toCSG()
                      		.movez(-heightInc)
			)
		}
		return CSG.unionAll(cylParts).hull().toZMin()
	}
}
File dir = new File(System.getProperty("java.io.tmpdir"))

File topSTL = new File(dir.getAbsolutePath()+"/CaseTop.stl")
File botSTL = new File(dir.getAbsolutePath()+"/CaseBottom.stl")

if(!topSTL.exists()||!botSTL.exists()) {
	println "Producing Case STL part"
	def parts = new BoardMaker().makeCase()
	
	for(CSG part:parts) {
		def filename=dir.getAbsolutePath()+"/"+part.getName()+".stl"
		println "Writing STL cache "+filename
		FileUtil.write(Paths.get(filename),
			part.toStlString());
	}
}else {
	println "Loading Cas parts from STL"
}

CSG top = Vitamins.get(topSTL);
CSG bot = Vitamins.get(botSTL);
top.setName("CaseTop")
bot.setName("CaseBottom")
bot.setManufacturing({ toMfg ->
	return toMfg
			.toXMin()
			.toYMin()
			.toZMin()
})
top.setManufacturing({ toMfg ->
	return toMfg
			.toXMin()
			.toYMin()
			.rotx(-180)
			.toZMin()
})
LengthParameter tailLength		= new LengthParameter("Cable Cut Out Length",30,[500, 0.01])
tailLength.setMM(25)
CSG motor = Vitamins.get("hobbyServo", "tproSG90")
CSG horn = Vitamins.get("hobbyServoHorn", "tproSG90_1")
			.movez(motor.getMaxZ())
double hornDepth = horn.getTotalZ()
double halfServoDistance = motor.getTotalX()/2
CSG tire = Vitamins.get("oRing", "2inchOD")

CSG wheepCore = new Cylinder(tire.getTotalZ()/2-3, hornDepth).toCSG()
				.roty(90)
				.moveToCenterX()
				.difference(tire)
				.roty(-90)
				.toZMin()
				.movez(motor.getMaxZ())
CSG tireAlligned  = tire.roty(90)
				.movez(motor.getMaxZ()+wheepCore.getTotalZ()/2)
				
Transform leftSide  = new Transform()
						.movez(-8)
						.movey(10)
						.roty(90)
						.movex(bot.getMinX())
						.movez(bot.getMinZ()-halfServoDistance)
						.movey(5)
						
Transform rightSide  = new Transform()
						.movez(-8)
						.movey(10)
						.roty(-90)
						.movex(bot.getMaxX())
						.movez(bot.getMinZ()-halfServoDistance)
						.movey(5)
						
CSG asmOfDrive = motor
					//.union(horn)
					.rotz(180)

CSG leftDrive = asmOfDrive.transformed(leftSide)
				
CSG rightDrive = asmOfDrive.transformed(rightSide)
CSG leftDriveHorn = horn.transformed(leftSide)

CSG rightDriveHorn = horn.transformed(rightSide)
				
CSG bothDrive = leftDrive.union(rightDrive)


				
CSG leftWheel =  wheepCore.transformed(leftSide).difference(leftDriveHorn)
CSG rightWheel =  wheepCore.transformed(rightSide).difference(rightDriveHorn)
leftDriveHorn.setColor(Color.BLACK)
	.setManufacturing({ toMfg ->
	return null
})
rightDriveHorn.setColor(Color.BLACK)
	.setManufacturing({ toMfg ->
	return null
})
CSG tireMovedR = tireAlligned.transformed(rightSide).setColor(Color.BLACK)
tireMovedR.setManufacturing({ toMfg ->
	return null
})
CSG tireMovedL = tireAlligned.transformed(leftSide).setColor(Color.BLACK)
tireMovedL.setManufacturing({ toMfg ->
	return null
})
bothDrive.setColor(Color.BLUE)
bothDrive.setManufacturing({ toMfg ->
	return null
})
CSG servoBlock = new Cube(bot.getTotalX()-caseRounding*2,bot.getTotalY()-caseRounding*2, bothDrive.getTotalZ())
					.toCSG()		
					.toZMax()
					.movez(bot.getMinZ())
					.toYMax()
					.movey(bot.getMaxY()-caseRounding)
					.toXMax()
					.movex(bot.getMaxX()-caseRounding)
CSG servoCover = servoBlock.difference(servoBlock.movez(2))
					.movez(-2)
double servoCoverSurfaceDistance  = servoCover.getMinZ()
double distanceFromBottomToGround= Math.abs(tireMovedR.getMinZ()- servoCoverSurfaceDistance)
CSG Caster= new Sphere(distanceFromBottomToGround,20,30).toCSG()

Caster=Caster.difference(Caster.getBoundingBox().toZMin())
			.movez(servoCover.getMinZ())
			.movex(servoCover.getCenterX())
			.toYMax()
			.movey(servoCover.getMaxY())

CSG blockCordCut = servoBlock.toYMax()
						.movey(servoBlock.getMinY()+12)
						.movez(-5)
	
		
CSG coverscrew = Vitamins.get("chamferedScrew", "M3x16")
					.rotx(180)
					.toZMin()
					.movez(servoCoverSurfaceDistance)
					.movex(servoCover.getMinX())
double InsetScrew = 10
CSG screws = coverscrew.movex(InsetScrew).union(coverscrew.movex(servoCover.getTotalX()-InsetScrew))
				.movey(servoCover.getTotalY()/2)

double batterySunkIn =10
CSG NineVolt = Vitamins.get("BatteryBox", "9vbattery")
CSG batteryHolder = NineVolt.getBoundingBox().scalex(1.15)
						.scalez(1.2)
						.scaley(1.15)
						.toYMin()
						.movey(NineVolt.getMinY())
batteryHolder=batteryHolder.intersect(batteryHolder.movez(-batterySunkIn))	
					.difference(NineVolt)					

Transform tf9v = new Transform()
			.movex(servoCover.getCenterX())
			.movey(-NineVolt.getMinY()-servoCover.getMinY())
			.movez(-NineVolt.getMaxZ()+servoCoverSurfaceDistance+batterySunkIn)


NineVolt=NineVolt.transformed(tf9v)
batteryHolder=batteryHolder.transformed(tf9v)

servoCover=servoCover
	.union(Caster)
	.union(batteryHolder)
	.difference(NineVolt)
	.difference(screws)
bot=bot.union(servoBlock)
.difference(bothDrive)
.difference(blockCordCut)
.difference(NineVolt)
.difference(screws)

servoCover.setName("servoCover")
.setManufacturing({ toMfg ->
	return toMfg.roty(180).toZMin()
})

leftWheel.setName("leftWheel")
.setManufacturing({ toMfg ->
	return toMfg.roty(-90).toZMin()
})

rightWheel.setName("rightWheel")
.setManufacturing({ toMfg ->
	return toMfg.roty(90).toZMin()
})
bot
.setManufacturing({ toMfg ->
	return toMfg.toZMin()
})
NineVolt.setColor(Color.SILVER)
.setManufacturing({ toMfg ->
	return null
})

screws.setColor(Color.SILVER)
.setManufacturing({ toMfg ->
	return null
})


top.addAssemblyStep(2, new Transform().movez(30))

bothDrive.addAssemblyStep(3, new Transform().movez(-30))
servoCover.addAssemblyStep(4, new Transform().movez(-40))
screws.addAssemblyStep(5, new Transform().movez(-60))
NineVolt.addAssemblyStep(6, new Transform().movey(-60))

leftWheel.addAssemblyStep(8, new Transform().movex(-30))
tireMovedL.addAssemblyStep(8, new Transform().movex(-30))
tireMovedL.addAssemblyStep(7, new Transform().movex(-10))
leftDriveHorn.addAssemblyStep(9, new Transform().movex(-40))

rightWheel.addAssemblyStep(8, new Transform().movex(30))
tireMovedR.addAssemblyStep(8, new Transform().movex(30))
tireMovedR.addAssemblyStep(7, new Transform().movex(10))
rightDriveHorn.addAssemblyStep(9, new Transform().movex(40))

return [top,bot,servoCover,bothDrive,leftWheel,rightWheel,tireMovedR,tireMovedL,NineVolt,screws,rightDriveHorn,leftDriveHorn]




