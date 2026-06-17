import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://gvgkrmuzooqbfkagizgk.supabase.co'
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd2Z2tybXV6b29xYmZrYWdpemdrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODE3MTU5NjEsImV4cCI6MjA5NzI5MTk2MX0.LaabBdiY7Fq45Ds_x8qY5c4koWapRURgCa1H9ac2oIM'

export const supabase = createClient(supabaseUrl, supabaseAnonKey)
