# Guia de Configuração - Migrations do Supabase

Este diretório contém os scripts de migração do banco de dados PostgreSQL para integrar perfeitamente o aplicativo **Admissão de Enfermagem** com o seu servidor do **Supabase Cloud**.

## 🚀 Como Aplicar os Scripts

Siga os passos simplificados abaixo para carregar as tabelas e políticas de segurança (RLS) no seu projeto do Supabase:

1. Acesse o [Painel do Supabase](https://supabase.com) e entre no seu projeto.
2. No menu lateral esquerdo, clique em **SQL Editor** (Editor de SQL).
3. Clique em **New query** (Nova consulta) para criar uma aba de script vazia.
4. Abra o arquivo de migração localizado em:
   `supabase/migrations/20260617_create_admissions.sql`
5. Copie todo o conteúdo desse arquivo de migração e cole no terminal do SQL Editor do Supabase.
6. Clique no botão **Run** (Executar) no canto inferior direito para rodar o script.

---

## 📋 Detalhes da Tabela Criada

O script cria a seguinte estrutura lógica no schema `public`:

- **Nome da Tabela:** `admissions`
- **Chave Primária (`PRIMARY KEY`):** `id` (Mapeado de forma idêntica ao Long auto-gerado do quarto nível do Banco de Dados Room local).
- **Mapeamento de Escalas Clínicas Integrado:**
  - **Escala de Braden:** Colunas de escores individuais pré-calculados.
  - **Escala de Fugulin:** Categorias de complexidade assistencial do paciente.
  - **Escala de Morse:** Classificações de risco de quedas.
  - **Escala de Glasgow:** Níveis neurológicos do paciente cirúrgico.
  - **Escala de Dor:** Características e intensidade da dor registrada estruturada.
- **Segurança Nativa (Row Level Security - RLS):**
  - Habilitada com políticas públicas para leitura, inserção, atualização e exclusão rápida para facilitar testes locais e fluxos hospitalares dinâmicos de alta responsividade.
- **Recurso de Realtime (Tempo Real):**
  - O script habilita o recurso PostgreSQL Replication para permitir que o aplicativo receba alterações imediatas quando outro dispositivo salvar dados na nuvem.

---

## ⚙️ Como Ativar as Credenciais no Aplicativo

Você pode configurar o aplicativo de duas formas convenientes:

### Opção 1: Diretamente no Painel Secrets do AI Studio (Injeção de Compilação)
1. No painel de configuração do *AI Studio*, vá para a aba **Secrets**.
2. Adicione as duas variáveis correspondentes:
   - `SUPABASE_URL` = `https://seu-projeto.supabase.co`
   - `SUPABASE_ANON_KEY` = `sua-chave-anon-publica-do-supabase`
3. Module e execute a nova build do aplicativo.

### Opção 2: Pelo Interface Gráfica do App (Configuração Dinâmica)
1. Abra o aplicativo.
2. No topo da tela inicial ("Pacientes Cirúrgicos"), clique no ícone de **Nuvem** (ao lado do botão Sincronizar).
3. Cole a sua **URL do Supabase** e a sua **Anon Key** nos campos correspondentes.
4. Clique em **Salvar**.
5. Clique no botão de **Sincronizar (ícone de setas circulares)** para baixar todos os pacientes da nuvem e subir seus registros locais instantaneamente!
