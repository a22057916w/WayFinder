<?php
$DBNAME = "v4_wayfinder";
$DBUSER = "root";
$DBPASSWD = "YtS0vamSHkmK7IAg#";
$DBHOST = "localhost";

$floor = (string)$_POST['floor'];
$number = (string)$_POST['number'];
//$room2 = (string)$_POST['room2'];

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
$floorNumber = $floor;
$poster_table_temp = 'sf0f_poster';
$poster_table = str_replace("0" , "$floorNumber" , $poster_table_temp);

$poster_coordinateANDrotation_sql ="SELECT coordinate, rotation
									FROM `$poster_table`
									WHERE id = '$number'";
	$result = mysqli_query($conn, $poster_coordinateANDrotation_sql);
	if($result)
	{	
		while($row = mysqli_fetch_array($result))
		{
			$poster_coordinateANDrotation[] = $row;
		};
		$newkey = "poster_coordinate";
		$oldkey = "coordinate";		
		$poster_coordinateANDrotation[0][$newkey] = $poster_coordinateANDrotation[0][$oldkey];
		unset($poster_coordinateANDrotation[0][$oldkey]);
		$newkey1 = "poster_rotation";
		$oldkey1 = "rotation";		
		$poster_coordinateANDrotation[0][$newkey1] = $poster_coordinateANDrotation[0][$oldkey1];
		unset($poster_coordinateANDrotation[0][$oldkey1]);
		print(json_encode($poster_coordinateANDrotation,JSON_UNESCAPED_UNICODE));
	}
?>
<form action="poster.php" method="post">
floor:
<input type ="text" name="floor" value="">
number:
<input type ="text" name="number" value="">
<input type ="submit" value="送出">
</form>