<?php
$DBNAME = "v4_wayfinder";
$DBUSER = "root";
$DBPASSWD = "YtS0vamSHkmK7IAg#";
$DBHOST = "localhost";

$floor = (string)$_POST['floor'];
$number = (string)$_POST['number'];
$room2 = (string)$_POST['room2'];

$conn = mysqli_connect( $DBHOST, $DBUSER , $DBPASSWD);
if (empty($conn)){
  print mysqli_error($conn);
  die ("無法連結資料庫");
  exit;
}
if( !mysqli_select_db($conn, $DBNAME)) {
  die ("無法選擇資料庫");
}

// 設定連線編碼
mysqli_query( $conn, "SET NAMES utf8");

$vertex2_id = array();
$countvertex2_id = 0;   
$floorNumber = $floor;								//起點的樓層 sf"2"33
$target_room_id = $room2;
$floorNumber2temp1= preg_split('//',$room2,-1,PREG_SPLIT_NO_EMPTY);
$floorNumber2 = $floorNumber2temp1[2];				//終點的樓層 sf"4"05
$sfloor_index_array = array();
$tfloor_index_array = array();
$dist_array = array();

$floor_table_temp = 'sf_0f';
$vertex_table_temp = 'sf0f_vertex';
$next_table_temp = 'sf0f_next';
$dist_table_temp = 'sf0f_dist';
$poster_table_temp = 'sf0f_poster';
$poster_table = str_replace("0" , "$floorNumber" , $poster_table_temp);
$floor_table = str_replace("0" , "$floorNumber" , $floor_table_temp);
$start_dist_table = str_replace("0" , "$floorNumber" , $dist_table_temp);
$vertex_table = str_replace("0" , "$floorNumber" , $vertex_table_temp);
$next_table = str_replace("0" , "$floorNumber" , $next_table_temp);
$floor2_table = str_replace("0" , "$floorNumber2" , $floor_table_temp);	
$vertex2_table = str_replace("0" , "$floorNumber2" , $vertex_table_temp);
$next2_table = str_replace("0" , "$floorNumber2" , $next_table_temp);

$elevator_string = "elevator";

if($floorNumber2temp1[0]!="s"){
	include 'path_toilet.php';
}
else{
	//海報的vertex
	$poster_vertex_id_sql = "SELECT vertex_id
							 FROM `$poster_table`
							 WHERE id = $number";

	$result = mysqli_query($conn, $poster_vertex_id_sql);
	if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$temp1 = $row[0];
			}
			$poster_vertex_id = $temp1;
			//print($poster_vertex_id);
		}

	$poster_vertex_index_sql = "SELECT _index_
								FROM `$vertex_table`
								WHERE ID = '$poster_vertex_id'";
		$result1 = mysqli_query($conn, $poster_vertex_index_sql);

		if($result1)
		{ 
			while($row1 = mysqli_fetch_array($result1))
			{			
				array_push($sfloor_index_array,$row1[0]);
			}
			//print($sfloor_index_array[0]);
		}	

	//終點的vertex
	$vertex2_id_sql = "SELECT vertex_id
						FROM `$floor2_table`
						WHERE id = '$room2'";

	$result = mysqli_query($conn, $vertex2_id_sql);
	if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$temp1 = preg_split('[\']',$row[0]);
			}
			$counttemp1 = count($temp1);
			for($i=1;$i<$counttemp1;$i=$i+2){
				array_push($vertex2_id,$temp1[$i]);
			}
			//print_r($vertex2_id);
			$countvertex2_id = count($vertex2_id);
			
		}
	$i = 0;
	while($countvertex2_id>0){
		$vertex_index_sql = "SELECT _index_
							FROM `$vertex2_table`
							WHERE ID = '$vertex2_id[$i]'";
		$result1 = mysqli_query($conn, $vertex_index_sql);

		if($result1)
		{ 
			while($row1 = mysqli_fetch_array($result1))
			{
				array_push($tfloor_index_array,$row1[0]);
			}
			//print_r($tfloor_index_array);
		}
		$countvertex2_id--;
		$i++;
	}
	//找最短路徑的兩個vertex
	$floortemp = (int)$floorNumber2 -1;
	$shortest_path = 1000000;
		for($j=0;$j<count($tfloor_index_array);$j++){
			$shortest_path_sql = "	SELECT dist
									FROM `$start_dist_table`
									WHERE start = '$sfloor_index_array[0]' 
									AND floor = '$floortemp'
									AND dest = '$tfloor_index_array[$j]'";
			$result = mysqli_query($conn, $shortest_path_sql);
			if($result)
			{	
				while($row = mysqli_fetch_array($result))
				{
					$temp = (double)$row[0];	
						//print($temp);					
					if($temp<$shortest_path){	
						$shortest_path=$temp;
						$shortest_path_start_vertex_index = $sfloor_index_array[0];
						$shortest_path_target_vertex_index = $tfloor_index_array[$j];
					}
				}
			}
		}
	if($floorNumber==$floorNumber2){
		$coordinate_sql = "	SELECT coordinate, rotation
							FROM `$next_table`
							WHERE start = '$shortest_path_start_vertex_index' 
							AND dest = '$shortest_path_target_vertex_index'";

		$result = mysqli_query($conn, $coordinate_sql);
		if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$coordinate[] = $row;
			}
			
		}
		$next_firstpoint_coordinate_sql = "SELECT coordinate, rotation
										FROM `$vertex_table`
										WHERE _index_ = '$sfloor_index_array[0]'";
		$result = mysqli_query($conn, $next_firstpoint_coordinate_sql);
		if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$next_firstpoint_coordinate[] = $row;
			}
			
			//print($next_firstpoint_coordinate[0][$key]);
		}
		$key = "coordinate";
		$key2 = "rotation";
		
		$coordinate[0][$key2] = substr($coordinate[0][$key2],1,strlen($coordinate[0][$key2]));
		$coordinate[0][$key2] = "', ".$coordinate[0][$key2];
		$coordinate[0][$key2] = $next_firstpoint_coordinate[0][$key2].$coordinate[0][$key2];
		$coordinate[0][$key2] = "['".$coordinate[0][$key2];
		
		$coordinate[0][$key] = substr($coordinate[0][$key],1,strlen($coordinate[0][$key]));
		$coordinate[0][$key] = ", ".$coordinate[0][$key];
		$coordinate[0][$key] = $next_firstpoint_coordinate[0][$key].$coordinate[0][$key];
		$coordinate[0][$key] = "[".$coordinate[0][$key];
		
		$poster_coordinate_sql = "SELECT coordinate, rotation
									FROM `$poster_table`
									WHERE id = $number";

		$result = mysqli_query($conn, $poster_coordinate_sql);
		if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$poster_coordinate[] = $row;
			}
		}
		//print($poster_coordinate[0][$key]);
		$coordinate[0][$key2] = substr($coordinate[0][$key2],1,strlen($coordinate[0][$key2]));
		$coordinate[0][$key2] = "', ".$coordinate[0][$key2];
		$coordinate[0][$key2] = $poster_coordinate[0][$key2].$coordinate[0][$key2];
		$coordinate[0][$key2] = "['".$coordinate[0][$key2];
		
		$coordinate[0][$key] = substr($coordinate[0][$key],1,strlen($coordinate[0][$key]));
		$coordinate[0][$key] = ", ".$coordinate[0][$key];
		$coordinate[0][$key] = $poster_coordinate[0][$key].$coordinate[0][$key];
		$coordinate[0][$key] = "[".$coordinate[0][$key];
		
		print(json_encode($coordinate,JSON_UNESCAPED_UNICODE));
	}
	else
	{
		//幾號電梯
		$elevator_sql = "SELECT elevator
						 FROM `$start_dist_table`
						 WHERE start = '$shortest_path_start_vertex_index' 
						 AND floor = '$floortemp' 
						 AND dest = '$shortest_path_target_vertex_index'";
		$result = mysqli_query($conn, $elevator_sql);
		if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$elevator = $row[0];
			}
		}
		$elevator = substr( $elevator , 0 , 1); //電梯號碼
		$elevator_vertex_sql = "SELECT vertex_id
								FROM `$floor_table`
								WHERE id = '$elevator'
								AND type = '$elevator_string'";
		$result = mysqli_query($conn, $elevator_vertex_sql);
		if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$temp = preg_split('[\']',$row[0]);  //$temp[1] = 我要的vertex_id
			}
		}
		$elevator_index_sql = "SELECT _index_
								FROM `$vertex_table`
								WHERE ID = '$temp[1]'";
		$result1 = mysqli_query($conn, $elevator_index_sql);
		if($result1)
		{ 
			while($row1 = mysqli_fetch_array($result1))
			{
				$start_elevator_index = $row1[0];
			}
		}
		//起點到電梯的coordinate rotation
		$startfloor_coordinate_sql = "SELECT coordinate, rotation
										FROM `$next_table`
										WHERE start = '$sfloor_index_array[0]' 
										AND dest = '$start_elevator_index'";
		$result = mysqli_query($conn, $startfloor_coordinate_sql);
		if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$start_coordinate[] = $row;
			}
			
		}
		$next_firstpoint_coordinate_sql = "SELECT coordinate, rotation
										FROM `$vertex_table`
										WHERE _index_ = '$sfloor_index_array[0]'";
		$result = mysqli_query($conn, $next_firstpoint_coordinate_sql);
		if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$next_firstpoint_coordinate[] = $row;
			}
			
			//print($next_firstpoint_coordinate[0][$key]);
		}
		$key = "coordinate";
		$key2 = "rotation";
		
		$start_coordinate[0][$key2] = substr($start_coordinate[0][$key2],1,strlen($start_coordinate[0][$key2]));
		$start_coordinate[0][$key2] = "', ".$start_coordinate[0][$key2];
		$start_coordinate[0][$key2] = $next_firstpoint_coordinate[0][$key2].$start_coordinate[0][$key2];
		$start_coordinate[0][$key2] = "['".$start_coordinate[0][$key2];
		
		$start_coordinate[0][$key] = substr($start_coordinate[0][$key],1,strlen($start_coordinate[0][$key]));
		$start_coordinate[0][$key] = ", ".$start_coordinate[0][$key];
		$start_coordinate[0][$key] = $next_firstpoint_coordinate[0][$key].$start_coordinate[0][$key];
		$start_coordinate[0][$key] = "[".$start_coordinate[0][$key];
		
		$poster_coordinate_sql = "SELECT coordinate, rotation
									FROM `$poster_table`
									WHERE id = $number";

		$result = mysqli_query($conn, $poster_coordinate_sql);
		if($result)
		{	
			while($row = mysqli_fetch_array($result))
			{
				$poster_coordinate[] = $row;
			}
		}
		$start_coordinate[0][$key2] = substr($start_coordinate[0][$key2],1,strlen($start_coordinate[0][$key2]));
		$start_coordinate[0][$key2] = "', ".$start_coordinate[0][$key2];
		$start_coordinate[0][$key2] = $poster_coordinate[0][$key2].$start_coordinate[0][$key2];
		$start_coordinate[0][$key2] = "['".$start_coordinate[0][$key2];
		
		$start_coordinate[0][$key] = substr($start_coordinate[0][$key],1,strlen($start_coordinate[0][$key]));
		$start_coordinate[0][$key] = ", ".$start_coordinate[0][$key];
		$start_coordinate[0][$key] = $poster_coordinate[0][$key].$start_coordinate[0][$key];
		$start_coordinate[0][$key] = "[".$start_coordinate[0][$key];
		
		
		print(json_encode($start_coordinate,JSON_UNESCAPED_UNICODE));
	}
}
?>
<form action="path_v5.php" method="post">
floor:
<input type ="text" name="floor" value="">
number:
<input type ="text" name="number" value="">
room2:
<input type ="text" name="room2" value="">
<input type ="submit" value="送出">
</form>