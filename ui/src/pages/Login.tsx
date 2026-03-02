import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { login } from "../services/auth.service";
import { useAuth } from "../store/auth";

const schema = z.object({
  username: z.string().min(1, "Username required"),
  password: z.string().min(1, "Password required"),
});
type FormData = z.infer<typeof schema>;

export default function Login() {
  const nav = useNavigate();
  const { setSession } = useAuth();
  const [error, setError] = useState<string | null>(null);

  const form = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { username: "", password: "" },
  });

  const onSubmit = async (values: FormData) => {
    setError(null);
    try {
      const res = await login(values);
      setSession(res.token, res.user ?? { username: values.username });
      nav("/dashboard");
    } catch (e: any) {
      setError(e?.response?.data?.message ?? "Login failed");
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <h1>Login</h1>

      <form onSubmit={form.handleSubmit(onSubmit)}>
        <div>
          <input placeholder="username" {...form.register("username")} />
          <p>{form.formState.errors.username?.message}</p>
        </div>

        <div>
          <input type="password" placeholder="password" {...form.register("password")} />
          <p>{form.formState.errors.password?.message}</p>
        </div>

        {error && <p style={{ color: "red" }}>{error}</p>}

        <button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Logging in..." : "Login"}
        </button>
      </form>
    </div>
  );
}