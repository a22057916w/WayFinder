<?php
$DBNAME = "wayfinder";
$DBUSER = "root";
$DBPASSWD = "YtS0vamSHkmK7IAg#";
$DBHOST = "localhost";

//$floor = (string)$_POST['floor'];
//$number = (string)$_POST['number'];
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

$target_room_id = $room2;
$floorNumber2temp1= preg_split('//',$room2);
$floorNumber2 = $floorNumber2temp1[3];				//終點的樓層 sf"4"05
$floor_table_temp = 'sf_0f';
$floor2_table = str_replace("0" , "$floorNumber2" , $floor_table_temp);	


$target_vertexANDdoor_sql ="SELECT vertex, door
								FROM `$floor2_table`
								WHERE id = '$target_room_id'";
	$result = mysqli_query($conn, $target_vertexANDdoor_sql);
	if($result)
	{	
		while($row = mysqli_fetch_array($result))
		{
			$target_vertexANDdoor[] = $row;
		};	
		$newkey = "dest_coordinate";
		$oldkey = "door";		
		$target_vertexANDdoor[0][$newkey] = $target_vertexANDdoor[0][$oldkey];
		unset($target_vertexANDdoor[0][$oldkey]);
		print(json_encode($target_vertexANDdoor,JSON_UNESCAPED_UNICODE));
	}
?>
<form action="dest.php" method="post">
room2:
<input type ="text" name="room2" value="">
<input type ="submit" value="送出">
</form>