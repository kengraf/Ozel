<?php
$headers = getallheaders();
$content = file_get_contents('php://input');
file_put_contents('upload/'.$headers['X_FILENAME'], $content);
?>
