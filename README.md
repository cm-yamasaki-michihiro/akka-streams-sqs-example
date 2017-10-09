# Akka Streamsで構築したSQSからメッセージを取得するジョブワーカーのサンプル

## ローカル実行
1. Elastic MQ起動
   ```shell
   docker-compose up -d
   ```

2. MQにメッセージを送信し続けるワーカー起動
   ```shell
   sbt run
   ```

   MainとSqsMessageSenderのどちらを実行するかという旨のメッセージが表示されるのでSqsMessageSenbderの番号を入力

3. MQからメッセージを取得するワーカー起動

   ```shell
   sbt run
   ```

   MainとSqsMessageSenderのどちらを実行するかという旨のメッセージが表示されるのでMainの番号を入力
