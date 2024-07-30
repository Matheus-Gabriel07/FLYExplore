# FLYExplore
O FLYExplore é um aplicativo fácil de usar que concede aos usuários acesso a informações de voos. Tendo ideia feita a partir do desafio final de [Persistência de Dados](https://developer.android.com/codelabs/basic-android-kotlin-compose-flight-search?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-3&hl=pt-br#0). Este aplicativo permite que os usuários encontrem opções de voos adequadas inserindo consultas de pesquisa, fornecendo recomendações de voos provenientes de um banco de dados. Os usuários podem explorar os voos disponíveis ao selecionar um aeroporto específico e salvá-los com um único clique para recuperação futura.

## Data Storage
O aplicativo utiliza dois mecanismos principais de armazenamento de dados:
- **Room Database** - Room é usado para gerenciar com eficiência o armazenamento de informações de voo. Este banco de dados é a espinha dorsal do aplicativo, facilitando a recuperação e armazenamento de dados de voo.
- **DataStore** - O DataStore fornece uma solução confiável para salvar e recuperar as preferências do usuário, incluindo suas entradas na barra de pesquisa.
