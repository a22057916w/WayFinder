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
$floorNumber2temp1= preg_split('//',$room2);
$floorNumber2 = $floorNumber2temp1[3];				//終點的樓層 sf"4"05
$sfloor_index_array = array();
$tfloor_index_array = array();
$dist_array = array();
$target_door_lng = array();
$target_door_lat = array();


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
		$shortest_path_sql = 
					"SELECT dist
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
	//print($shortest_path_target_vertex_index );

$target_vertex_sql ="SELECT coordinate
						FROM `$vertex2_table`
						WHERE _index_ = '$shortest_path_target_vertex_index'";

$result = mysqli_query($conn, $target_vertex_sql);
if($result)
	{	
		while($row = mysqli_fetch_array($result))
		{
			$temp2 = preg_split('[,]',$row[0]);
 		}
		$target_lng = substr($temp2[0],1,strlen($temp2[0]));
		$target_lat = substr($temp2[1],0,-1);
		//print_r($temp2);
	}
$target_door_sql ="SELECT door
					FROM `$floor2_table`
					WHERE id = '$target_room_id'";
	$result = mysqli_query($conn, $target_door_sql);
	if($result)
	{	
		while($row = mysqli_fetch_array($result))
		{
			$temp3 = preg_split('[\']',$row[0]);
		};
		$counttemp3 = count($temp3);
		for($i=1;$i<$counttemp3;$i=$i+4){
			array_push($target_door_lng,$temp3[$i]);
		}
		for($i=3;$i<$counttemp3;$i=$i+4){
			array_push($target_door_lat,$temp3[$i]);
		}
		//print_r($temp3);
		//print_r($target_door_lng);
		//print_r($target_door_lat);
		//print(json_encode($target_door,JSON_UNESCAPED_UNICODE));
	}
$countdoor = count($target_door_lng);
$distance = array();
for($i=0;$i<$countdoor;$i=$i+1)
{
	$distance[$i] = GreatCircle($target_lng,$target_lat,$target_door_lng[$i],$target_door_lng[$i]);
}
$min_distance_number = 0;
for($i=1;$i<$countdoor;$i=$i+1)
{
	if($distance[$i]<$distance[$min_distance_number]){
		$min_distance_number = $i;
	}
}
$min_distance_door = $target_door_lng[$min_distance_number].",";
$min_distance_door = $min_distance_door.$target_door_lat[$min_distance_number];
$print_door = array("dest_coordinate"=>$min_distance_door);
$temp_string = "[".json_encode($print_door ,JSON_UNESCAPED_UNICODE);
$temp_string = $temp_string."]";
print($temp_string);

function GreatCircle($a_lng,$a_lat,$b_lng,$b_lat){
	$x1 = deg2rad($a_lng);
	$y1 = deg2rad($a_lat);
	$x2 = deg2rad($b_lng);
	$y2 = deg2rad($b_lat);
	
	$a =  pow(sin(($x2-$x1)/2), 2) + cos($x1)*cos($x2)* pow(sin(($y2-$y1)/2), 2);
	$angle1 = 2 * asin(min(1,sqrt($a)));
	$angle1 = rad2deg($angle1);
	$distance1 = 60 * $angle1;
	
	return $distance1;
}	
	
	
?>
<form action="dest_test.php" method="post">
floor:
<input type ="text" name="floor" value="">
number:
<input type ="text" name="number" value="">
room2:
<input type ="text" name="room2" value="">
<input type ="submit" value="送出">
</form>